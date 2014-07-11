package de.subcentral.core.model.subtitle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import com.google.common.base.Objects;

import de.subcentral.core.model.Contribution;
import de.subcentral.core.model.Work;
import de.subcentral.core.model.media.AvMediaItem;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.release.AbstractRelease;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.MediaRelease;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.util.SimplePropertyDescriptor;

public class SubtitleRelease extends AbstractRelease<Subtitle> implements Work
{
	public static final String						PROP_NAME_COMPATIBLE_MEDIA_RELEASES	= "compatibleMediaReleases";
	public static final String						PROP_NAME_CONTRIBUTIONS				= "contributions";

	public static final SimplePropertyDescriptor	PROP_NAME							= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_NAME);
	public static final SimplePropertyDescriptor	PROP_MATERIALS						= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_MATERIALS);
	public static final SimplePropertyDescriptor	PROP_GROUP							= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_GROUP);
	public static final SimplePropertyDescriptor	PROP_TAGS							= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_TAGS);
	public static final SimplePropertyDescriptor	PROP_COMPATIBLE_MEDIA_RELEASES		= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_COMPATIBLE_MEDIA_RELEASES);
	public static final SimplePropertyDescriptor	PROP_DATE							= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_DATE);
	public static final SimplePropertyDescriptor	PROP_SECTION						= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_SECTION);
	public static final SimplePropertyDescriptor	PROP_SIZE							= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_SIZE);
	public static final SimplePropertyDescriptor	PROP_NUKE_REASON					= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_NUKE_REASON);
	public static final SimplePropertyDescriptor	PROP_INFO							= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_INFO);
	public static final SimplePropertyDescriptor	PROP_INFO_URL						= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_INFO_URL);
	public static final SimplePropertyDescriptor	PROP_SOURCE							= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_SOURCE);
	public static final SimplePropertyDescriptor	PROP_SOURCE_URL						= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_SOURCE_URL);
	public static final SimplePropertyDescriptor	PROP_CONTRIBUTIONS					= new SimplePropertyDescriptor(SubtitleRelease.class,
																								PROP_NAME_CONTRIBUTIONS);

	public static final String						CONTRIBUTION_TYPE_ADJUSTMENT		= "ADJUSTMENT";
	public static final String						CONTRIBUTION_TYPE_CUSTOMIZATION		= "CUSTOMIZATION";

	public static SubtitleRelease create(MediaRelease compatibleMediaRelease, String subtitleLanguage, String group, String... tags)
	{
		return create(null, compatibleMediaRelease, subtitleLanguage, group, tags);
	}

	public static SubtitleRelease create(String name, MediaRelease compatibleMediaRelease, String subtitleLanguage, String group, String... tags)
	{
		SubtitleRelease rls = new SubtitleRelease();
		rls.setName(name);
		rls.setCompatibleMediaRelease(compatibleMediaRelease);
		List<Subtitle> subs = new ArrayList<>(compatibleMediaRelease.getMaterials().size());
		for (Media media : compatibleMediaRelease.getMaterials())
		{
			subs.add(new Subtitle((AvMediaItem) media, subtitleLanguage));
		}
		rls.setMaterials(subs);
		if (group != null)
		{
			rls.setGroup(new Group(group));
		}
		rls.setTags(Releases.tags(tags));
		return rls;
	}

	private Set<MediaRelease>	compatibleMediaReleases	= new HashSet<>(2);
	private List<Contribution>	contributions			= new ArrayList<>(2);

	public SubtitleRelease()
	{

	}

	public SubtitleRelease(String name)
	{
		this.name = name;
	}

	public SubtitleRelease(String name, Set<MediaRelease> compatibleMediaReleases, Subtitle material, Group group, List<Tag> tags)
	{
		this.name = name;
		setCompatibleMediaReleases(compatibleMediaReleases);
		setMaterial(material);
		this.group = group;
		setTags(tags);

	}

	public SubtitleRelease(String name, Set<MediaRelease> compatibleMediaReleases, List<Subtitle> materials, Group group, List<Tag> tags)
	{
		this.name = name;
		setCompatibleMediaReleases(compatibleMediaReleases);
		setMaterials(materials);
		this.group = group;
		setTags(tags);

	}

	public Set<MediaRelease> getCompatibleMediaReleases()
	{
		return compatibleMediaReleases;
	}

	public MediaRelease getFirstCompatibleMediaRelease()
	{
		return compatibleMediaReleases.isEmpty() ? null : compatibleMediaReleases.iterator().next();
	}

	public void setCompatibleMediaReleases(Set<MediaRelease> compatibleMediaReleases)
	{
		Validate.notNull(compatibleMediaReleases, "compatibleMediaReleases cannot be null");
		this.compatibleMediaReleases = compatibleMediaReleases;
	}

	@Override
	public List<Contribution> getContributions()
	{
		return contributions;
	}

	public void setContributions(List<Contribution> contributions)
	{
		Validate.notNull(contributions, "contributions cannot be null");
		this.contributions = contributions;
	}

	// Convenience
	public void setCompatibleMediaRelease(MediaRelease compatibleMediaRelease)
	{
		this.compatibleMediaReleases = new HashSet<>(1);
		this.compatibleMediaReleases.add(compatibleMediaRelease);
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("name", name)
				.add("materials", materials)
				.add("group", group)
				.add("tags", tags)
				.add("compatibleMediaReleases", compatibleMediaReleases)
				.add("date", date)
				.add("section", section)
				.add("size", size)
				.add("nukeReason", nukeReason)
				.add("info", info)
				.add("infoUrl", infoUrl)
				.add("source", source)
				.add("sourceUrl", sourceUrl)
				.add("contributions", contributions)
				.toString();
	}
}