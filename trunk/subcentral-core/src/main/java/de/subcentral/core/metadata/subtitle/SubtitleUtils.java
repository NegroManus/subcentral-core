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

import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.SubtitleAdjustmentNamer;
import de.subcentral.core.standardizing.StandardizingChange;

public class SubtitleUtils
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

	public static void standardizeTags(Subtitle sub, List<StandardizingChange> changes)
	{
		if (sub == null || sub.getTags().isEmpty())
		{
			return;
		}
		boolean tagsChanged = false;
		List<Tag> oldTags = ImmutableList.copyOf(sub.getTags());

		Pattern pVersion = Pattern.compile("V(\\d+)", Pattern.CASE_INSENSITIVE);
		Matcher mVersion = pVersion.matcher("");
		ListIterator<Tag> iter = sub.getTags().listIterator();
		while (iter.hasNext())
		{
			Tag tag = iter.next();
			if (Subtitle.TAG_HEARING_IMPAIRED.equals(tag))
			{
				boolean oldHIValue = sub.isHearingImpaired();
				sub.setHearingImpaired(true);
				if (oldHIValue != true)
				{
					changes.add(new StandardizingChange(sub, Subtitle.PROP_HEARING_IMPAIRED.getPropName(), oldHIValue, true));
				}

				iter.remove();
				tagsChanged = true;
			}
			else if (mVersion.reset(tag.getName()).matches())
			{
				String oldVersion = sub.getVersion();
				String newVersion = mVersion.group(1);
				sub.setVersion(newVersion);
				if (!Objects.equals(oldVersion, newVersion))
				{
					changes.add(new StandardizingChange(sub, Subtitle.PROP_VERSION.getPropName(), oldVersion, newVersion));
				}

				iter.remove();
				tagsChanged = true;
			}
		}
		if (tagsChanged)
		{
			changes.add(new StandardizingChange(sub, Subtitle.PROP_TAGS.getPropName(), oldTags, sub.getTags()));
		}
	}

	public static boolean containsHearingImpairedTag(List<Tag> tags)
	{
		return tags.contains(Subtitle.TAG_HEARING_IMPAIRED);
	}

	private SubtitleUtils()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

}
