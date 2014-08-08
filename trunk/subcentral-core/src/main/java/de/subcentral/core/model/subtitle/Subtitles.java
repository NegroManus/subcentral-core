package de.subcentral.core.model.subtitle;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.SubtitleAdjustmentNamer;

public class Subtitles
{
	public Set<String> generateNames(SubtitleAdjustment subAdj, NamingService namingService)
	{
		ImmutableSet.Builder<String> names = ImmutableSet.builder();
		for (Release rls : subAdj.getMatchingReleases())
		{
			names.add(namingService.name(subAdj, ImmutableMap.of(SubtitleAdjustmentNamer.PARAM_KEY_RELEASE, rls)));
		}
		return names.build();
	}

	public static Consumer<Subtitle> getSubtitleTagsNormalizer()
	{
		return Subtitles::normalizeTags;
	}

	public static Subtitle normalizeTags(Subtitle subtitle)
	{
		Pattern pVersion = Pattern.compile("V(\\d+)", Pattern.CASE_INSENSITIVE);
		Matcher mVersion = pVersion.matcher("");
		ListIterator<Tag> iter = subtitle.getTags().listIterator();
		while (iter.hasNext())
		{
			Tag tag = iter.next();
			if (Subtitle.TAG_HEARING_IMPAIRED.equals(tag))
			{
				subtitle.setHearingImpaired(true);
				iter.remove();
				continue;
			}
			if (mVersion.reset(tag.getName()).matches())
			{
				subtitle.setVersion(Integer.parseInt(mVersion.group(1)));
				iter.remove();
				continue;
			}
		}
		return subtitle;
	}

	public static boolean containsHearingImpairedTag(List<Tag> tags)
	{
		return tags.contains(Subtitle.TAG_HEARING_IMPAIRED);
	}

	private Subtitles()
	{
		// utility class
	}

}
