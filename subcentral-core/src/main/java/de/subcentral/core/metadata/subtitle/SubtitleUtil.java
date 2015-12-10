package de.subcentral.core.metadata.subtitle;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.correction.Correction;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.SubtitleReleaseNamer;

public class SubtitleUtil
{
	public static Set<String> buildNamesForMatchingReleases(SubtitleRelease subAdj, NamingService namingService)
	{
		if (subAdj == null || subAdj.getMatchingReleases().isEmpty())
		{
			return ImmutableSet.of();
		}
		ImmutableSet.Builder<String> names = ImmutableSet.builder();
		for (Release rls : subAdj.getMatchingReleases())
		{
			names.add(namingService.name(subAdj, ImmutableMap.of(SubtitleReleaseNamer.PARAM_RELEASE, rls)));
		}
		return names.build();
	}

	public static List<Media> getMediaFromSubtitles(SubtitleRelease subAdj)
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

	public static void standardizeTags(SubtitleRelease subAdj, List<Correction> changes)
	{
		if (subAdj == null || subAdj.getTags().isEmpty())
		{
			return;
		}
		boolean tagsChanged = false;
		List<Tag> oldTags = ImmutableList.copyOf(subAdj.getTags());

		Matcher mVersion = Pattern.compile("V(\\d+)", Pattern.CASE_INSENSITIVE).matcher("");
		ListIterator<Tag> iter = subAdj.getTags().listIterator();
		while (iter.hasNext())
		{
			Tag tag = iter.next();
			if (mVersion.reset(tag.getName()).matches())
			{
				String oldRev = subAdj.getVersion();
				String newRev = mVersion.group(1);
				subAdj.setVersion(newRev);
				if (!Objects.equals(oldRev, newRev))
				{
					changes.add(new Correction(subAdj, SubtitleRelease.PROP_VERSION.getPropName(), oldRev, newRev));
				}
				iter.remove();
				tagsChanged = true;
			}
		}
		if (tagsChanged)
		{
			changes.add(new Correction(subAdj, SubtitleRelease.PROP_TAGS.getPropName(), oldTags, subAdj.getTags()));
		}
	}

	private SubtitleUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

}
