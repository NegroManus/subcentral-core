package de.subcentral.core.model.subtitle;

import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Contribution;
import de.subcentral.core.model.Contributor;
import de.subcentral.core.model.Models;
import de.subcentral.core.model.Prop;
import de.subcentral.core.model.Work;
import de.subcentral.core.model.media.AvMediaItem;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Nuke;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.util.ListComparator;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleAdjustment implements Work, Comparable<SubtitleAdjustment>
{
	public static final SimplePropDescriptor	PROP_SUBTITLES					= new SimplePropDescriptor(SubtitleAdjustment.class, Prop.SUBTITLES);
	public static final SimplePropDescriptor	PROP_MATCHING_RELEASES			= new SimplePropDescriptor(SubtitleAdjustment.class,
																						Prop.MATCHING_RELEASES);
	public static final SimplePropDescriptor	PROP_DATE						= new SimplePropDescriptor(SubtitleAdjustment.class, Prop.DATE);
	public static final SimplePropDescriptor	PROP_SIZE						= new SimplePropDescriptor(SubtitleAdjustment.class, Prop.SIZE);
	public static final SimplePropDescriptor	PROP_FILECOUNT					= new SimplePropDescriptor(SubtitleAdjustment.class, Prop.FILE_COUNT);
	public static final SimplePropDescriptor	PROP_NUKES						= new SimplePropDescriptor(SubtitleAdjustment.class, Prop.NUKES);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS				= new SimplePropDescriptor(SubtitleAdjustment.class,
																						Prop.CONTRIBUTIONS);

	public static final String					CONTRIBUTION_TYPE_ADJUSTMENT	= "ADJUSTMENT";

	// In 99,9% of the cases, there is only one subtitle
	private final List<Subtitle>				subtitles						= new ArrayList<>(1);
	// Most adjustments are compatible to 1-2 releases. Maybe 5-6 sometimes
	// HashMap / HashSet initial capacities should be a power of 2
	private final Set<Release>					matchingReleases				= new HashSet<>(2);
	private Temporal							date;
	private long								size							= 0L;
	private int									fileCount						= 0;
	private final List<Nuke>					nukes							= new ArrayList<>(0);
	// In 99,9% of the cases, there is only one adjustment contribution
	private final List<Contribution>			contributions					= new ArrayList<>(1);

	public static SubtitleAdjustment create(Release matchingRelease, String language, String group)
	{
		SubtitleAdjustment subAdjustment = new SubtitleAdjustment();
		Group grp = null;
		if (group != null)
		{
			grp = new Group(group);
		}
		for (Media media : matchingRelease.getMedia())
		{
			Subtitle sub = new Subtitle();
			sub.setMediaItem((AvMediaItem) media);
			sub.setLanguage(language);
			if (grp != null)
			{
				sub.setGroup(grp);
			}
			subAdjustment.getSubtitles().add(sub);
		}
		subAdjustment.getMatchingReleases().add(matchingRelease);
		return subAdjustment;
	}

	public SubtitleAdjustment()
	{

	}

	public SubtitleAdjustment(Subtitle subtitle, Release matchingRelease)
	{
		this.subtitles.add(subtitle);
		this.matchingReleases.add(matchingRelease);
	}

	public SubtitleAdjustment(Subtitle subtitle, Set<Release> matchingReleases)
	{
		this.subtitles.add(subtitle);
		this.matchingReleases.addAll(matchingReleases);
	}

	public SubtitleAdjustment(List<Subtitle> subtitles, Release matchingRelease)
	{
		this.subtitles.addAll(subtitles);
		this.matchingReleases.add(matchingRelease);
	}

	public SubtitleAdjustment(List<Subtitle> subtitles, Set<Release> matchingReleases)
	{
		this.subtitles.addAll(subtitles);
		this.matchingReleases.addAll(matchingReleases);
	}

	// Properties
	public List<Subtitle> getSubtitles()
	{
		return subtitles;
	}

	public void setSubtitles(List<Subtitle> subtitles)
	{
		Validate.notNull(subtitles, "subtitles cannot be null");
		this.subtitles.clear();
		this.subtitles.addAll(subtitles);
	}

	public Set<Release> getMatchingReleases()
	{
		return matchingReleases;
	}

	public void setMatchingReleases(Set<Release> matchingReleases)
	{
		Validate.notNull(matchingReleases, "matchingReleases cannot be null");
		this.matchingReleases.clear();
		this.matchingReleases.addAll(matchingReleases);
	}

	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date) throws IllegalArgumentException
	{
		Models.validateTemporalClass(date);
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

	public int getFileCount()
	{
		return fileCount;
	}

	public void setFileCount(int fileCount)
	{
		this.fileCount = fileCount;
	}

	public List<Nuke> getNukes()
	{
		return nukes;
	}

	public void setNukes(List<Nuke> nukes)
	{
		Validate.notNull(nukes, "nukes cannot be null");
		this.nukes.clear();
		this.nukes.addAll(nukes);
	}

	@Override
	public List<Contribution> getContributions()
	{
		return contributions;
	}

	public void setContributions(List<Contribution> contributions)
	{
		Validate.notNull(contributions, "contributions cannot be null");
		this.contributions.clear();
		this.contributions.addAll(contributions);
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
		Validate.notNull(subtitle, "subtitle cannot be null");
		this.subtitles.clear();
		this.subtitles.add(subtitle);
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
		Validate.notNull(matchingRelease, "matchingRelease cannot be null");
		this.matchingReleases.clear();
		this.matchingReleases.add(matchingRelease);
	}

	public boolean isNuked()
	{
		return !nukes.isEmpty();
	}

	public void nuke(String nukeReason)
	{
		nukes.add(new Nuke(nukeReason));
	}

	public void nuke(String nukeReason, Temporal date)
	{
		nukes.add(new Nuke(nukeReason, date));
	}

	public void nukeNow(String nukeReason)
	{
		nukes.add(new Nuke(nukeReason, ZonedDateTime.now()));
	}

	public void addAdjuster(Contributor contributor)
	{
		contributions.add(new Contribution(CONTRIBUTION_TYPE_ADJUSTMENT, contributor, 1L, 1d, null));
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj != null && SubtitleAdjustment.class.equals(obj.getClass()))
		{
			SubtitleAdjustment o = (SubtitleAdjustment) obj;
			return new EqualsBuilder().append(subtitles, o.subtitles).append(matchingReleases, o.matchingReleases).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(33, 51).append(subtitles).append(matchingReleases).toHashCode();
	}

	@Override
	public int compareTo(SubtitleAdjustment o)
	{
		if (o == null)
		{
			return -1;
		}
		return ComparisonChain.start()
				.compare(subtitles, o.subtitles, ListComparator.<Subtitle> create())
				.compare(getFirstMatchingRelease(), o.getFirstMatchingRelease(), Settings.createDefaultOrdering())
				.result();
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(SubtitleAdjustment.class)
				.omitNullValues()
				.add("subtitles", Models.nullIfEmpty(subtitles))
				.add("matchingReleases", Models.nullIfEmpty(matchingReleases))
				.add("date", date)
				.add("size", size)
				.add("fileCount", fileCount)
				.add("nukes", nukes)
				.add("contributions", Models.nullIfEmpty(contributions))
				.toString();
	}
}
