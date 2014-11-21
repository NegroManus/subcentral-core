package de.subcentral.core.model.subtitle;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.SubtitleAdjustmentNamer;
import de.subcentral.core.standardizing.StandardizingChange;

public class Subtitles
{
	public static Set<String> buildNamesForMatchingReleases(SubtitleAdjustment subAdj, NamingService namingService)
	{
		if (subAdj == null || subAdj.getMatchingReleases().isEmpty())
		{
			return ImmutableSet.of();
		}
		ImmutableSet.Builder<String> names = ImmutableSet.builder();
		for (Release rls : subAdj.getMatchingReleases())
		{
			names.add(namingService.name(subAdj, ImmutableMap.of(SubtitleAdjustmentNamer.PARAM_RELEASE, rls)));
		}
		return names.build();
	}

	public static List<Media> getMediaFromSubtitles(SubtitleAdjustment subAdj)
	{
		if (subAdj == null || subAdj.getSubtitles().isEmpty())
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<Media> media = ImmutableList.builder();
		for (Subtitle sub : subAdj.getSubtitles())
		{
			media.add(sub.getMedia());
		}
		return media.build();
	}

	public static List<StandardizingChange> standardizeTags(Subtitle sub)
	{
		if (sub == null || sub.getTags().isEmpty())
		{
			return ImmutableList.of();
		}
		boolean changed = false;
		List<Tag> oldTags = ImmutableList.copyOf(sub.getTags());

		Pattern pVersion = Pattern.compile("V(\\d+)", Pattern.CASE_INSENSITIVE);
		Matcher mVersion = pVersion.matcher("");
		ListIterator<Tag> iter = sub.getTags().listIterator();
		while (iter.hasNext())
		{
			Tag tag = iter.next();
			if (Subtitle.TAG_HEARING_IMPAIRED.equals(tag))
			{
				sub.setHearingImpaired(true);
				iter.remove();
				changed = true;
			}
			else if (mVersion.reset(tag.getName()).matches())
			{
				sub.setVersion(Integer.parseInt(mVersion.group(1)));
				iter.remove();
				changed = true;
			}
		}
		return changed ? ImmutableList.of(new StandardizingChange(sub, Subtitle.PROP_TAGS.getPropName(), oldTags, sub.getTags()))
				: ImmutableList.of();
	}

	public static boolean containsHearingImpairedTag(List<Tag> tags)
	{
		return tags.contains(Subtitle.TAG_HEARING_IMPAIRED);
	}

	private Subtitles()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore should not be instantiated.");
	}

}
