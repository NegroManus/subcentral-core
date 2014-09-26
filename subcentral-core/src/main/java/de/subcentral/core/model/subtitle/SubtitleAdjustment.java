package de.subcentral.core.model.subtitle;

import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Contribution;
import de.subcentral.core.model.Contributor;
import de.subcentral.core.model.Models;
import de.subcentral.core.model.PropNames;
import de.subcentral.core.model.Work;
import de.subcentral.core.model.media.AvMedia;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Nuke;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.util.ListComparator;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleAdjustment implements Work, Comparable<SubtitleAdjustment>
{
	public static final SimplePropDescriptor	PROP_NAME						= new SimplePropDescriptor(SubtitleAdjustment.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_SUBTITLES					= new SimplePropDescriptor(SubtitleAdjustment.class,
																						PropNames.SUBTITLES);
	public static final SimplePropDescriptor	PROP_MATCHING_RELEASES			= new SimplePropDescriptor(SubtitleAdjustment.class,
																						PropNames.MATCHING_RELEASES);
	public static final SimplePropDescriptor	PROP_DATE						= new SimplePropDescriptor(SubtitleAdjustment.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_SIZE						= new SimplePropDescriptor(SubtitleAdjustment.class, PropNames.SIZE);
	public static final SimplePropDescriptor	PROP_FILECOUNT					= new SimplePropDescriptor(SubtitleAdjustment.class,
																						PropNames.FILE_COUNT);
	public static final SimplePropDescriptor	PROP_NUKES						= new SimplePropDescriptor(SubtitleAdjustment.class, PropNames.NUKES);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS				= new SimplePropDescriptor(SubtitleAdjustment.class,
																						PropNames.CONTRIBUTIONS);

	public static final String					CONTRIBUTION_TYPE_ADJUSTMENT	= "ADJUSTMENT";

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
			sub.setMedia((AvMedia) media);
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

	private String						name;
	// In 99,9% of the cases, there is only one subtitle
	private final List<Subtitle>		subtitles			= new ArrayList<>(1);
	// Most adjustments are compatible to 1-2 releases. Maybe 5-6 sometimes
	// HashMap / HashSet initial capacities should be a power of 2
	private final Set<Release>			matchingReleases	= new HashSet<>(2);
	private Temporal					date;
	private long						size				= 0L;
	private int							fileCount			= 0;
	private final List<Nuke>			nukes				= new ArrayList<>(0);
	// In 99,9% of the cases, there is only one adjustment contribution
	private final List<Contribution>	contributions		= new ArrayList<>(1);

	public SubtitleAdjustment()
	{

	}

	public SubtitleAdjustment(Subtitle subtitle, Release matchingRelease)
	{
		setSingleSubtitle(subtitle);
		setSingleMatchingRelease(matchingRelease);
	}

	public SubtitleAdjustment(Subtitle subtitle, Collection<Release> matchingReleases)
	{
		setSingleSubtitle(subtitle);
		setMatchingReleases(matchingReleases);
	}

	public SubtitleAdjustment(List<Subtitle> subtitles, Release matchingRelease)
	{
		setSubtitles(subtitles);
		setSingleMatchingRelease(matchingRelease);
	}

	public SubtitleAdjustment(List<Subtitle> subtitles, Collection<Release> matchingReleases)
	{
		setSubtitles(subtitles);
		setMatchingReleases(matchingReleases);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	// Properties
	public List<Subtitle> getSubtitles()
	{
		return subtitles;
	}

	public void setSubtitles(List<Subtitle> subtitles)
	{
		Validate.noNullElements(subtitles);
		this.subtitles.clear();
		this.subtitles.addAll(subtitles);
	}

	public Set<Release> getMatchingReleases()
	{
		return matchingReleases;
	}

	public void setMatchingReleases(Collection<? extends Release> matchingReleases)
	{
		Validate.noNullElements(matchingReleases);
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
		Validate.noNullElements(nukes);
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
		Validate.noNullElements(contributions);
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
		this.subtitles.clear();
		if (subtitle != null)
		{
			this.subtitles.add(subtitle);
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
		this.matchingReleases.clear();
		if (matchingRelease != null)
		{
			this.matchingReleases.add(matchingRelease);
		}
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
		if (obj != null && getClass().equals(obj.getClass()))
		{
			SubtitleAdjustment o = (SubtitleAdjustment) obj;
			if (name != null && o.name != null)
			{
				return name.equals(o.name);
			}
			return new EqualsBuilder().append(subtitles, o.subtitles).append(matchingReleases, o.matchingReleases).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		if (name != null)
		{
			return new HashCodeBuilder(33, 55).append(name).toHashCode();
		}
		return new HashCodeBuilder(33, 51).append(subtitles).append(matchingReleases).toHashCode();
	}

	@Override
	public int compareTo(SubtitleAdjustment o)
	{
		if (o == null)
		{
			return -1;
		}
		if (name != null && o.name != null)
		{
			return Settings.STRING_ORDERING.compare(name, name);
		}
		return ComparisonChain.start()
				.compare(subtitles, o.subtitles, ListComparator.<Subtitle> create())
				.compare(getFirstMatchingRelease(), o.getFirstMatchingRelease(), Settings.createDefaultOrdering())
				.result();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(SubtitleAdjustment.class)
				.omitNullValues()
				.add("name", name)
				.add("subtitles", Models.nullIfEmpty(subtitles))
				.add("matchingReleases", Models.nullIfEmpty(matchingReleases))
				.add("date", date)
				.add("size", size)
				.add("fileCount", fileCount)
				.add("nukes", Models.nullIfEmpty(nukes))
				.add("contributions", Models.nullIfEmpty(contributions))
				.toString();
	}
}
