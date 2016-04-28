package de.subcentral.core.correct;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.metadata.subtitle.SubtitleUtil;

public class CorrectionDefaults
{
	public static final Function<String, String>	ALNUM_BLANK_REPLACER					= new CharStringReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ", "'´`", ' ');
	public static final Function<String, String>	ALNUM_DOT_HYPEN_REPLACER				= new CharStringReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.-", "'´`", '.');
	/**
	 * Use this for media naming. <br/>
	 * hyphen "-" has to be allowed, so that media names like "How.I.Met.Your.Mother.S09E01-E24" are possible also release names like "Katy.Perry-The.Prismatic.World.Tour" are common
	 */
	public static final Function<String, String>	ALNUM_DOT_UNDERSCORE_HYPHEN_REPLACER	= new CharStringReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.-_", "'´`", '.');
	public static final Function<String, String>	HYPHEN_CLEANER							= new StringReplacer(".-", "-").andThen(new StringReplacer("-.", "-"))
			.andThen(new PatternStringReplacer(Pattern.compile("-+"), "-"));
	public static final Function<String, String>	AND_REPLACER							= new StringReplacer("&", "and");
	public static final Function<String, String>	ACCENT_REPLACER							= StringUtils::stripAccents;
	public static final Function<String, String>	TO_LOWERCASE_REPLACER					= StringUtils::lowerCase;

	private static final TypeBasedCorrectionService	DEFAULT_CORRECTION_SERVICE				= createDefaultCorrectionService();

	private CorrectionDefaults()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	private static TypeBasedCorrectionService createDefaultCorrectionService()
	{
		TypeBasedCorrectionService service = new TypeBasedCorrectionService("default");
		registerAllDefaultNestedBeansRetrievers(service);
		registerAllDefaultCorrectors(service);
		return service;
	}

	public static CorrectionService getDefaultCorrectionService()
	{
		return DEFAULT_CORRECTION_SERVICE;
	}

	public static Iterable<?> retrieveNestedBeans(Series series)
	{
		return series.getNetworks();
	}

	public static Iterable<?> retrieveNestedBeans(Season season)
	{
		if (season.getSeries() != null)
		{
			return ImmutableList.of(season.getSeries());
		}
		return ImmutableList.of();
	}

	public static Iterable<?> retrieveNestedBeans(Episode epi)
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

	public static Iterable<?> retrieveNestedBeans(Release rls)
	{
		return rls.getMedia();
	}

	public static Iterable<?> retrieveNestedBeans(Subtitle sub)
	{
		if (sub.getMedia() != null)
		{
			return ImmutableList.of(sub.getMedia());
		}
		return ImmutableList.of();
	}

	public static Iterable<?> retrieveNestedBeans(SubtitleRelease subAdj)
	{
		List<Object> nestedBeans = new ArrayList<>(subAdj.getSubtitles().size() + subAdj.getMatchingReleases().size());
		nestedBeans.addAll(subAdj.getSubtitles());
		nestedBeans.addAll(subAdj.getMatchingReleases());
		return nestedBeans;
	}

	public static void registerAllDefaultNestedBeansRetrievers(TypeBasedCorrectionService service)
	{
		service.registerNestedBeansRetriever(Series.class, CorrectionDefaults::retrieveNestedBeans);
		service.registerNestedBeansRetriever(Season.class, CorrectionDefaults::retrieveNestedBeans);
		service.registerNestedBeansRetriever(Episode.class, CorrectionDefaults::retrieveNestedBeans);
		service.registerNestedBeansRetriever(Release.class, CorrectionDefaults::retrieveNestedBeans);
		service.registerNestedBeansRetriever(Subtitle.class, CorrectionDefaults::retrieveNestedBeans);
		service.registerNestedBeansRetriever(SubtitleRelease.class, CorrectionDefaults::retrieveNestedBeans);
	}

	public static void registerAllDefaultCorrectors(TypeBasedCorrectionService service)
	{
		service.registerCorrector(SubtitleRelease.class, SubtitleUtil::standardizeTags);
		service.registerCorrector(Release.class, new ReleaseTagsCorrector(new TagsReplacer(Tag.list("AAC2", "0"), Tag.list("AAC2.0"))));
		service.registerCorrector(Release.class, new ReleaseTagsCorrector(new TagsReplacer(Tag.list("DD5", "1"), Tag.list("DD5.1"))));
		service.registerCorrector(Release.class, new ReleaseTagsCorrector(new TagsReplacer(Tag.list("H", "264"), Tag.list("H.264"))));
		service.registerCorrector(Release.class, new ReleaseTagsCorrector(new TagsReplacer(Tag.list("H", "265"), Tag.list("H.265"))));
		service.registerCorrector(Release.class, new ReleaseTagsCorrector(new TagsReplacer(Tag.list("H264"), Tag.list("H.264"))));
		service.registerCorrector(Release.class, new ReleaseTagsCorrector(new TagsReplacer(Tag.list("H265"), Tag.list("H.265"))));
	}
}