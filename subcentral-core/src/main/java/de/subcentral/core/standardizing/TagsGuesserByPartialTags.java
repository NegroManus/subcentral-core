package de.subcentral.core.standardizing;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.ReleaseUtils;
import de.subcentral.core.model.release.Tag;

public class TagsGuesserByPartialTags implements Standardizer<Release>
{
	private final ImmutableList<Tag>					partialTags;
	private final ImmutableList<Tag>					fullTags;
	private final Supplier<? extends Collection<Tag>>	metaTagsSupplier;

	public TagsGuesserByPartialTags(Collection<Tag> partialTags, Collection<Tag> fullTags, Supplier<? extends Collection<Tag>> metaTagsSupplier)
	{
		Objects.requireNonNull(partialTags, "partialTags");
		Objects.requireNonNull(fullTags, "fullTags");
		if (partialTags.isEmpty())
		{
			throw new IllegalArgumentException("partialTags cannot be empty");
		}
		if (fullTags.isEmpty())
		{
			throw new IllegalArgumentException("fullTags cannot be empty");
		}
		if (partialTags.equals(fullTags))
		{
			throw new IllegalArgumentException("partialTags cannot be equal to fullTags");
		}
		this.partialTags = ImmutableList.copyOf(partialTags);
		this.fullTags = ImmutableList.copyOf(fullTags);
		this.metaTagsSupplier = Objects.requireNonNull(metaTagsSupplier, "metaTagsSupplier");
	}

	public ImmutableList<Tag> getPartialTags()
	{
		return partialTags;
	}

	public ImmutableList<Tag> getFullTags()
	{
		return fullTags;
	}

	public Supplier<? extends Collection<Tag>> getMetaTagsSupplier()
	{
		return metaTagsSupplier;
	}

	@Override
	public List<StandardizingChange> standardize(Release rls) throws StandardizingException
	{
		if (rls == null)
		{
			return ImmutableList.of();
		}
		if (partialTags.equals(rls.getTags()))
		{
			ImmutableList<Tag> oldTags = ImmutableList.copyOf(rls.getTags());
			ReleaseUtils.replaceTags(rls, fullTags, metaTagsSupplier.get());
			// replaceTags() will always result in a change since partialTags != fullTags
			return ImmutableList.of(new StandardizingChange(rls, Release.PROP_TAGS.getPropName(), oldTags, rls.getTags()));
		}
		return ImmutableList.of();
	}
}
