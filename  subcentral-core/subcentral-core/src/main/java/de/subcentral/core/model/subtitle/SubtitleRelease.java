package de.subcentral.core.model.subtitle;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import com.google.common.base.Objects;

import de.subcentral.core.model.Contribution;
import de.subcentral.core.model.Models;
import de.subcentral.core.model.Prop;
import de.subcentral.core.model.Work;
import de.subcentral.core.model.media.AvMediaItem;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleRelease implements Work
{
	public static final SimplePropDescriptor	PROP_SUBTITLES					= new SimplePropDescriptor(SubtitleRelease.class, Prop.SUBTITLES);
	public static final SimplePropDescriptor	PROP_MATCHING_RELEASES			= new SimplePropDescriptor(SubtitleRelease.class,
																						Prop.MATCHING_RELEASES);
	public static final SimplePropDescriptor	PROP_DATE						= new SimplePropDescriptor(SubtitleRelease.class, Prop.DATE);
	public static final SimplePropDescriptor	PROP_SIZE						= new SimplePropDescriptor(SubtitleRelease.class, Prop.SIZE);
	public static final SimplePropDescriptor	PROP_NUKE_REASON				= new SimplePropDescriptor(SubtitleRelease.class, Prop.NUKE_REASON);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS				= new SimplePropDescriptor(SubtitleRelease.class, Prop.CONTRIBUTIONS);

	public static final String					CONTRIBUTION_TYPE_ADJUSTMENT	= "ADJUSTMENT";

	// In 99,9% of the cases, there is only one subtitle
	private List<Subtitle>						subtitles						= new ArrayList<>(1);
	// Most SubtitleReleases match 1 to 5 releases.
	private Set<Release>						matchingReleases				= new HashSet<>(5);
	private Temporal							date;
	private long								size;
	private String								nukeReason;
	// In 99,9% of the cases, there is only one adjustment contribution
	private List<Contribution>					contributions					= new ArrayList<>(1);

	public static SubtitleRelease create(Release matchingReleases, String language, String group)
	{
		SubtitleRelease subRls = new SubtitleRelease();
		List<Subtitle> subs = new ArrayList<>(matchingReleases.getMedia().size());
		for (Media media : matchingReleases.getMedia())
		{
			Subtitle sub = new Subtitle();
			sub.setMediaItem((AvMediaItem) media);
			sub.setLanguage(language);
			if (group != null)
			{
				sub.setGroup(new Group(group));
			}
		}
		subRls.setSingleMatchingRelease(matchingReleases);
		subRls.setSubtitles(subs);
		return subRls;
	}

	public SubtitleRelease()
	{

	}

	// Properties
	public List<Subtitle> getSubtitles()
	{
		return subtitles;
	}

	public void setSubtitles(List<Subtitle> subtitles)
	{
		Validate.notNull(subtitles, "subtitles cannot be null");
		this.subtitles = subtitles;
	}

	public Set<Release> getMatchingReleases()
	{
		return matchingReleases;
	}

	public void setMatchingReleases(Set<Release> matchingReleases)
	{
		Validate.notNull(matchingReleases, "matchingReleases cannot be null");
		this.matchingReleases = matchingReleases;
	}

	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date)
	{
		Models.validateDateClass(date);
		this.date = date;
	}

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	public String getNukeReason()
	{
		return nukeReason;
	}

	public void setNukeReason(String nukeReason)
	{
		this.nukeReason = nukeReason;
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
	public boolean containsSingleSubtitle()
	{
		return subtitles.size() == 1;
	}

	public Subtitle getFirstSubtitle()
	{
		return subtitles.isEmpty() ? null : subtitles.get(0);
	}

	public void setSingleSubtitle(Subtitle subtitle)
	{
		subtitles = new ArrayList<>(1);
		if (subtitle != null)
		{
			subtitles.add(subtitle);
		}
	}

	public boolean matchesSingleRelease()
	{
		return matchingReleases.size() == 1;
	}

	public Release getFirstMatchingRelease()
	{
		return matchingReleases.isEmpty() ? null : matchingReleases.iterator().next();
	}

	public void setSingleMatchingRelease(Release matchingRelease)
	{
		this.matchingReleases = new HashSet<>(1);
		if (matchingRelease != null)
		{
			this.matchingReleases.add(matchingRelease);
		}
	}

	public boolean isNuked()
	{
		return nukeReason != null;
	}

	// Object methods
	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("subtitles", subtitles)
				.add("matchingReleases", matchingReleases)
				.add("date", date)
				.add("size", size)
				.add("nukeReason", nukeReason)
				.add("contributions", contributions)
				.toString();
	}
}
