package de.subcentral.core.naming;

import de.subcentral.core.util.CharReplacer;

public class NamingStandards
{
	public static final String						DEFAULT_DOMAIN				= "scene";
	public static final char[]						DEFAULT_ALLOWED_CHARS		= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.-_".toCharArray();
	public static final String						DEFAULT_REPLACEMENT			= ".";
	public static final char[]						DEFAULT_CHARS_TO_DELETE		= "'´`".toCharArray();

	public static final CharReplacer				STANDARD_REPLACER			= new CharReplacer();

	public static final SeriesEpisodeNamer			SERIES_EPISODE_NAMER		= new SeriesEpisodeNamer();
	public static final MiniSeriesEpisodeNamer		MINI_SERIES_EPISODE_NAMER	= new MiniSeriesEpisodeNamer();
	public static final DatedEpisodeNamer			DATED_EPISODE_NAMER			= new DatedEpisodeNamer();
	public static final SeriesTypeAwareEpisodeNamer	EPISODE_NAMER				= new SeriesTypeAwareEpisodeNamer();
	public static final MovieNamer					MOVIE_NAMER					= new MovieNamer();
	public static final SubtitleNamer				SUBTITLE_NAMER				= new SubtitleNamer();
	public static final MediaReleaseNamer			MEDIA_RELEASE_NAMER			= new MediaReleaseNamer();
	public static final SubtitleReleaseNamer		SUBTITLE_RELEASE_NAMER		= new SubtitleReleaseNamer();
	public static final NamingServiceImpl			NAMING_SERVICE				= new NamingServiceImpl();
	static
	{
		STANDARD_REPLACER.setAllowedChars(DEFAULT_ALLOWED_CHARS);
		STANDARD_REPLACER.setReplacement(DEFAULT_REPLACEMENT);
		STANDARD_REPLACER.setCharsToDelete(DEFAULT_CHARS_TO_DELETE);

		NAMING_SERVICE.setDomain(DEFAULT_DOMAIN);
		NAMING_SERVICE.registerNamer(EPISODE_NAMER);
		NAMING_SERVICE.registerNamer(MOVIE_NAMER);
		NAMING_SERVICE.registerNamer(SUBTITLE_NAMER);
		NAMING_SERVICE.registerNamer(MEDIA_RELEASE_NAMER);
		NAMING_SERVICE.registerNamer(SUBTITLE_RELEASE_NAMER);
	}

	private NamingStandards()
	{
		// utility class
	}
}
