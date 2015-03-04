package de.subcentral.core.standardizing;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.metadata.subtitle.SubtitleUtil;

public class StandardizingDefaults
{
	private static final TypeStandardizingService	DEFAULT_STANDARDIZING_SERVICE	= new TypeStandardizingService("default");
	static
	{
		registerAllDefaultNestedBeansRetrievers(DEFAULT_STANDARDIZING_SERVICE);
		registerAllDefaulStandardizers(DEFAULT_STANDARDIZING_SERVICE);
	}

	public static StandardizingService getDefaultStandardizingService()
	{
		return DEFAULT_STANDARDIZING_SERVICE;
	}

	public static List<? extends Object> retrieveNestedBeans(Episode epi)
	{
		List<Object> nestedBeans = new ArrayList<>(2);
		if (epi.getSeries() != null)
		{
			nestedBeans.add(epi.getSeries());
		}
		if (epi.getSeason() != null)
		{
			nestedBeans.add(epi.getSeason());
		}
		return nestedBeans;
	}

	public static List<? extends Object> retrieveNestedBeans(Season season)
	{
		if (season.getSeries() != null)
		{
			return ImmutableList.of(season.getSeries());
		}
		return ImmutableList.of();
	}

	public static List<? extends Object> retrieveNestedBeans(Release rls)
	{
		return rls.getMedia();
	}

	public static List<? extends Object> retrieveNestedBeans(Subtitle sub)
	{
		if (sub.getMedia() != null)
		{
			return ImmutableList.of(sub.getMedia());
		}
		return ImmutableList.of();
	}

	public static List<? extends Object> retrieveNestedBeans(SubtitleAdjustment subAdj)
	{
		List<Object> nestedBeans = new ArrayList<>(subAdj.getSubtitles().size() + subAdj.getMatchingReleases().size());
		nestedBeans.addAll(subAdj.getSubtitles());
		nestedBeans.addAll(subAdj.getMatchingReleases());
		return nestedBeans;
	}

	public static void registerAllDefaultNestedBeansRetrievers(TypeStandardizingService service)
	{
		service.registerNestedBeansRetriever(Episode.class, StandardizingDefaults::retrieveNestedBeans);
		service.registerNestedBeansRetriever(Season.class, StandardizingDefaults::retrieveNestedBeans);
		service.registerNestedBeansRetriever(Release.class, StandardizingDefaults::retrieveNestedBeans);
		service.registerNestedBeansRetriever(Subtitle.class, StandardizingDefaults::retrieveNestedBeans);
		service.registerNestedBeansRetriever(SubtitleAdjustment.class, StandardizingDefaults::retrieveNestedBeans);
	}

	public static void registerAllDefaulStandardizers(TypeStandardizingService service)
	{
		service.registerStandardizer(Subtitle.class, SubtitleUtil::standardizeTags);
		service.registerStandardizer(Release.class, new ReleaseTagsStandardizer(Tag.list("AAC2", "0"), Tag.list("AAC2.0")));
		service.registerStandardizer(Release.class, new ReleaseTagsStandardizer(Tag.list("DD5", "1"), Tag.list("DD5.1")));
		service.registerStandardizer(Release.class, new ReleaseTagsStandardizer(Tag.list("H", "264"), Tag.list("H.264")));
		service.registerStandardizer(Release.class, new ReleaseTagsStandardizer(Tag.list("H", "265"), Tag.list("H.265")));
		service.registerStandardizer(Release.class, new ReleaseTagsStandardizer(Tag.list("WEB", "DL"), Tag.list("WEB-DL")));
	}

	private StandardizingDefaults()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
