package de.subcentral.core.standardizing;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.ReleaseUtils;
import de.subcentral.core.model.release.Tag;

public class TagsGuesserByGroup implements Standardizer<Release>
{
	private final Group									group;
	private final ImmutableList<Tag>					tagsForGroup;
	private final Supplier<? extends Collection<Tag>>	metaTagsSupplier;

	public TagsGuesserByGroup(Group group, Collection<Tag> tagsForGroup, Supplier<? extends Collection<Tag>> metaTagsSupplier)
	{
		Objects.requireNonNull(tagsForGroup, "tagsForGroup");
		if (tagsForGroup.isEmpty())
		{
			throw new IllegalArgumentException("tagsForGroup cannot be empty");
		}
		this.group = Objects.requireNonNull(group, "group");
		this.tagsForGroup = ImmutableList.copyOf(tagsForGroup);
		this.metaTagsSupplier = Objects.requireNonNull(metaTagsSupplier, "metaTagsSupplier");
	}

	public Group getGroup()
	{
		return group;
	}

	public ImmutableList<Tag> getTagsForGroup()
	{
		return tagsForGroup;
	}

	@Override
	public List<StandardizingChange> standardize(Release rls) throws StandardizingException
	{
		if (rls == null)
		{
			return ImmutableList.of();
		}

		// only if group matches and release has no (non-meta) tags yet
		if (group.equals(rls.getGroup()))
		{
			Collection<Tag> metaTags = metaTagsSupplier.get();
			List<Tag> tagsWithoutMetaTags = ReleaseUtils.copyWithoutMetaTags(rls.getTags(), metaTags);
			if (tagsWithoutMetaTags.isEmpty())
			{
				ImmutableList<Tag> oldTags = ImmutableList.copyOf(rls.getTags());
				ReleaseUtils.replaceTags(rls, tagsForGroup, metaTags);
				// replaceTags() will always result in a change since tags were empty
				return ImmutableList.of(new StandardizingChange(rls, Release.PROP_TAGS.getPropName(), oldTags, rls.getTags()));
			}
		}
		return ImmutableList.of();
	}
}
