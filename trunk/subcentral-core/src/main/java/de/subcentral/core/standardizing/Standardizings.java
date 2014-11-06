package de.subcentral.core.standardizing;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.model.subtitle.Subtitles;

public class Standardizings
{
	private static final ClassBasedStandardizingService	DEFAULT_STANDARDIZING_SERVICE	= new ClassBasedStandardizingService("default");
	static
	{
		registerAllDefaultNestedBeansRetrievers(DEFAULT_STANDARDIZING_SERVICE);
		registerAllDefaulStandardizers(DEFAULT_STANDARDIZING_SERVICE);
	}

	public static StandardizingService getDefaultStandardizingService()
	{
		return DEFAULT_STANDARDIZING_SERVICE;
	}

	public static <T> void mayStandardize(T entity, StandardizingService standardizingService)
	{
		if (standardizingService != null)
		{
			standardizingService.standardize(entity);
		}
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

	public static void registerAllDefaultNestedBeansRetrievers(ClassBasedStandardizingService service)
	{
		service.registerNestedBeansRetriever(Episode.class, Standardizings::retrieveNestedBeans);
		service.registerNestedBeansRetriever(Season.class, Standardizings::retrieveNestedBeans);
		service.registerNestedBeansRetriever(Release.class, Standardizings::retrieveNestedBeans);
		service.registerNestedBeansRetriever(Subtitle.class, Standardizings::retrieveNestedBeans);
		service.registerNestedBeansRetriever(SubtitleAdjustment.class, Standardizings::retrieveNestedBeans);
	}

	public static void registerAllDefaulStandardizers(ClassBasedStandardizingService service)
	{
		service.registerStandardizer(Subtitle.class, Subtitles::standardizeTags);
		service.registerStandardizer(Release.class, Releases::standardizeTags);
	}

	private Standardizings()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore should not be instantiated.");
	}
}
