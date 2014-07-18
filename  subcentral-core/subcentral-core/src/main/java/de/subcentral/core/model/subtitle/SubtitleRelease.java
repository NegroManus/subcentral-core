package de.subcentral.core.model.subtitle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.model.Contribution;
import de.subcentral.core.model.Prop;
import de.subcentral.core.model.Work;
import de.subcentral.core.model.media.AvMediaItem;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.release.AbstractRelease;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.MediaRelease;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.naming.NamingStandards;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleRelease extends AbstractRelease<Subtitle> implements Work
{
	public static final SimplePropDescriptor	PROP_MATERIALS					= new SimplePropDescriptor(SubtitleRelease.class, Prop.MATERIALS);
	public static final SimplePropDescriptor	PROP_MATCHING_MEDIA_RELEASES	= new SimplePropDescriptor(SubtitleRelease.class,
																						Prop.MATCHING_MEDIA_RELEASES);
	public static final SimplePropDescriptor	PROP_DATE						= new SimplePropDescriptor(SubtitleRelease.class, Prop.DATE);
	public static final SimplePropDescriptor	PROP_SIZE						= new SimplePropDescriptor(SubtitleRelease.class, Prop.SIZE);
	public static final SimplePropDescriptor	PROP_NUKE_REASON				= new SimplePropDescriptor(SubtitleRelease.class, Prop.NUKE_REASON);
	public static final SimplePropDescriptor	PROP_ADJUSTER					= new SimplePropDescriptor(SubtitleRelease.class, Prop.ADJUSTER);

	public static final String					SECTION_SUB						= "SUB";

	public static final String					CONTRIBUTION_TYPE_ADJUSTMENT	= "ADJUSTMENT";

	private Set<MediaRelease>					matchingMediaReleases			= new HashSet<>(2);
	private List<Contribution>					contributions					= new ArrayList<>();

	public static SubtitleRelease create(MediaRelease matchingMediaRelease, String language, String group)
	{
		SubtitleRelease subRls = new SubtitleRelease();
		List<Subtitle> subs = new ArrayList<>(matchingMediaRelease.getMaterials().size());
		for (Media media : matchingMediaRelease.getMaterials())
		{
			Subtitle sub = new Subtitle();
			sub.setMediaItem((AvMediaItem) media);
			sub.setLanguage(language);
			if (group != null)
			{
				sub.setGroup(new Group(group));
			}
		}
		subRls.setMatchingMediaRelease(matchingMediaRelease);
		subRls.setMaterials(subs);
		return subRls;
	}

	public SubtitleRelease()
	{

	}

	@Override
	public String getName()
	{
		return NamingStandards.SUBTITLE_RELEASE_NAMER.name(this, NamingStandards.NAMING_SERVICE);
	}

	// Properties
	public Set<MediaRelease> getMatchingMediaReleases()
	{
		return matchingMediaReleases;
	}

	public MediaRelease getFirstMatchingMediaRelease()
	{
		return matchingMediaReleases.isEmpty() ? null : matchingMediaReleases.iterator().next();
	}

	public void setMatchingMediaReleases(Set<MediaRelease> matchingMediaReleases)
	{
		Validate.notNull(matchingMediaReleases, "matchingMediaReleases cannot be null");
		this.matchingMediaReleases = matchingMediaReleases;
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

	// Release implementation
	@Override
	public Group getGroup()
	{
		Subtitle sub = getFirstMaterial();
		return sub != null ? sub.getGroup() : null;
	}

	@Override
	public List<Tag> getTags()
	{
		Subtitle sub = getFirstMaterial();
		return sub != null ? sub.getTags() : null;
	}

	@Override
	public String getSection()
	{
		return SECTION_SUB;
	}

	@Override
	public String getInfo()
	{
		Subtitle sub = getFirstMaterial();
		return sub != null ? sub.getInfo() : null;
	}

	@Override
	public String getInfoUrl()
	{
		Subtitle sub = getFirstMaterial();
		return sub != null ? sub.getInfoUrl() : null;
	}

	@Override
	public String getSource()
	{
		Subtitle sub = getFirstMaterial();
		return sub != null ? sub.getSource() : null;
	}

	@Override
	public String getSourceUrl()
	{
		Subtitle sub = getFirstMaterial();
		return sub != null ? sub.getSourceUrl() : null;
	}

	// Convenience
	public void setMaterial(Subtitle material)
	{
		this.materials = new ArrayList<>(1);
		if (material != null)
		{
			this.materials.add(material);
		}
	}

	public void setMatchingMediaRelease(MediaRelease matchingMediaRelease)
	{
		this.matchingMediaReleases = new HashSet<>(1);
		if (matchingMediaRelease != null)
		{
			this.matchingMediaReleases.add(matchingMediaRelease);
		}
	}

	public String getLanguage()
	{
		Subtitle firstSub = getFirstMaterial();
		return firstSub != null ? firstSub.getLanguage() : null;
	}

	public List<Contribution> getAllContributions()
	{
		ImmutableList.Builder<Contribution> cs = ImmutableList.builder();

		for (Subtitle sub : materials)
		{
			cs.addAll(sub.getContributions());
		}
		cs.addAll(contributions);
		return cs.build();
	}

	public List<String> getAllInfos()
	{
		if (materials.isEmpty())
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<String> nfos = ImmutableList.builder();
		for (Subtitle sub : materials)
		{
			if (sub.getInfo() != null)
			{
				nfos.add(sub.getInfo());
			}
		}
		return nfos.build();
	}

	public List<String> getAllInfoUrls()
	{
		if (materials.isEmpty())
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<String> nfoUrls = ImmutableList.builder();
		for (Subtitle sub : materials)
		{
			if (sub.getInfoUrl() != null)
			{
				nfoUrls.add(sub.getInfoUrl());
			}
		}
		return nfoUrls.build();
	}

	public List<String> getAllSources()
	{
		if (materials.isEmpty())
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<String> srcs = ImmutableList.builder();
		for (Subtitle sub : materials)
		{
			if (sub.getSource() != null)
			{
				srcs.add(sub.getSource());
			}
		}
		return srcs.build();
	}

	public List<String> getAllSourceUrls()
	{
		if (materials.isEmpty())
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<String> srcUrls = ImmutableList.builder();
		for (Subtitle sub : materials)
		{
			if (sub.getSourceUrl() != null)
			{
				srcUrls.add(sub.getSourceUrl());
			}
		}
		return srcUrls.build();
	}

	// Object methods
	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("materials", materials)
				.add("matchingMediaReleases", matchingMediaReleases)
				.add("date", date)
				.add("size", size)
				.add("nukeReason", nukeReason)
				.add("contributions", contributions)
				.toString();
	}
}
