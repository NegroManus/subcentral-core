package de.subcentral.core.correction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.metadata.subtitle.SubtitleUtil;

public class CorrectionDefaults
{
	public static final Function<String, String>	ALNUM_BLANK_REPLACER				= new CharStringReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ", "'´`", ' ');
	public static final Function<String, String>	ALNUM_DOT_HYPEN_REPLACER			= new CharStringReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.-", "'´`", '.');
	/**
	 * Use this for media naming. <br/>
	 * hyphen "-" has to be allowed, so that media names like "How.I.Met.Your.Mother.S09E01-E24" are possible also release names like "Katy.Perry-The.Prismatic.World.Tour" are common
	 */
	public static final Function<String, String>	ALNUM_DOT_HYPEN_UNDERSCORE_REPLACER	= new CharStringReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.-_", "'´`", '.');
	public static final Function<String, String>	DOT_HYPHEN_DOT_REPLACER				= new StringReplacer(".-", "-").andThen(new StringReplacer("-.", "-"));
	public static final Function<String, String>	AND_REPLACER						= new StringReplacer("&", "and");
	public static final Function<String, String>	ACCENT_REPLACER						= (String s) -> StringUtils.stripAccents(s);
	public static final Function<String, String>	TO_LOWERCASE_REPLACER				= (String s) -> StringUtils.lowerCase(s);

	private static final TypeCorrectionService DEFAULT_CORRECTION_SERVICE = new TypeCorrectionService("default");

	static
	{
		registerAllDefaultNestedBeansRetrievers(DEFAULT_CORRECTION_SERVICE);
		registerAllDefaultCorrectors(DEFAULT_CORRECTION_SERVICE);
	}

	public static CorrectionService getDefaultCorrectionService()
	{
		return DEFAULT_CORRECTION_SERVICE;
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

	public static void registerAllDefaultNestedBeansRetrievers(TypeCorrectionService service)
	{
		service.registerNestedBeansRetriever(Episode.class, CorrectionDefaults::retrieveNestedBeans);
		service.registerNestedBeansRetriever(Season.class, CorrectionDefaults::retrieveNestedBeans);
		service.registerNestedBeansRetriever(Release.class, CorrectionDefaults::retrieveNestedBeans);
		service.registerNestedBeansRetriever(Subtitle.class, CorrectionDefaults::retrieveNestedBeans);
		service.registerNestedBeansRetriever(SubtitleAdjustment.class, CorrectionDefaults::retrieveNestedBeans);
	}

	public static void registerAllDefaultCorrectors(TypeCorrectionService service)
	{
		service.registerStandardizer(SubtitleAdjustment.class, SubtitleUtil::standardizeTags);
		service.registerStandardizer(Release.class, new ReleaseTagsCorrector(new TagsReplacer(Tag.list("AAC2", "0"), Tag.list("AAC2.0"))));
		service.registerStandardizer(Release.class, new ReleaseTagsCorrector(new TagsReplacer(Tag.list("DD5", "1"), Tag.list("DD5.1"))));
		service.registerStandardizer(Release.class, new ReleaseTagsCorrector(new TagsReplacer(Tag.list("H", "264"), Tag.list("H.264"))));
		service.registerStandardizer(Release.class, new ReleaseTagsCorrector(new TagsReplacer(Tag.list("H", "265"), Tag.list("H.265"))));
		service.registerStandardizer(Release.class, new ReleaseTagsCorrector(new TagsReplacer(Tag.list("H264"), Tag.list("H.264"))));
		service.registerStandardizer(Release.class, new ReleaseTagsCorrector(new TagsReplacer(Tag.list("H265"), Tag.list("H.265"))));
		service.registerStandardizer(Release.class, new ReleaseTagsCorrector(new TagsReplacer(Tag.list("WEB", "DL"), Tag.list("WEB-DL"))));
	}

	private CorrectionDefaults()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}