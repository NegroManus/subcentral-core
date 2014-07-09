package de.subcentral.core.naming;

import de.subcentral.core.util.CharReplacer;

public class NamingStandards
{
	public static final String						DEFAULT_DOMAIN			= "scene";
	public static final CharReplacer				STANDARD_REPLACER		= new CharReplacer();

	public static final SeasonedEpisodeNamer		SEASONED_EPISODE_NAMER	= new SeasonedEpisodeNamer();
	public static final MultiEpisodeNamer			MULTI_EPISODE_NAMER		= new MultiEpisodeNamer();
	public static final MovieNamer					MOVIE_NAMER				= new MovieNamer();
	public static final SubtitleNamer				SUBTITLE_NAMER			= new SubtitleNamer();
	public static final MediaReleaseNamer			MEDIA_RELEASE_NAMER		= new MediaReleaseNamer();
	public static final SubtitleReleaseNamer	SUBTITLE_RELEASE_NAMER	= new SubtitleReleaseNamer();
	public static final SimpleNamingService			NAMING_SERVICE			= new SimpleNamingService();
	static
	{
		NAMING_SERVICE.setDomain(DEFAULT_DOMAIN);
		NAMING_SERVICE.registerNamer(SEASONED_EPISODE_NAMER);
		NAMING_SERVICE.registerNamer(MULTI_EPISODE_NAMER);
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
