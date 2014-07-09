package de.subcentral.core.naming;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.MediaRelease;
import de.subcentral.core.model.subtitle.SubtitleRelease;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.SeparatorDescriptor;

public class NamingStandards
{
	public static final String					DEFAULT_DOMAIN			= "scene";
	public static final CharReplacer			STANDARD_REPLACER		= new CharReplacer();

	public static final SeasonedEpisodeNamer	SEASONED_EPISODE_NAMER	= new SeasonedEpisodeNamer();
	public static final MultiEpisodeNamer		MULTI_EPISODE_NAMER		= new MultiEpisodeNamer();
	public static final MovieNamer				MOVIE_NAMER				= new MovieNamer();
	public static final SubtitleNamer			SUBTITLE_NAMER			= new SubtitleNamer();
	public static final MediaReleaseNamer		MEDIA_RELEASE_NAMER		= new MediaReleaseNamer();
	public static final SubtitleReleaseNamer	SUBTITLE_RELEASE_NAMER	= new SubtitleReleaseNamer();
	public static final SimpleNamingService		NAMING_SERVICE			= new SimpleNamingService();
	static
	{
		try
		{
			Function<Group, String> groupToString = g -> g.getName().replaceAll("DIMENSION", "DiMENSION");

			MEDIA_RELEASE_NAMER.setPropertyToStringFunctions(ImmutableMap.of(new PropertyDescriptor("group", MediaRelease.class), groupToString));
			MEDIA_RELEASE_NAMER.setSeparators(ImmutableSet.of(SeparatorDescriptor.before(new PropertyDescriptor("group", MediaRelease.class), "-")));
			MEDIA_RELEASE_NAMER.setWholeNameOperator(STANDARD_REPLACER);

			SUBTITLE_RELEASE_NAMER.setSeparators(ImmutableSet.of(SeparatorDescriptor.before(new PropertyDescriptor("group", SubtitleRelease.class),
					"-")));
			SUBTITLE_RELEASE_NAMER.setWholeNameOperator(STANDARD_REPLACER);

			NAMING_SERVICE.setDomain(DEFAULT_DOMAIN);
			NAMING_SERVICE.registerNamer(SEASONED_EPISODE_NAMER);
			NAMING_SERVICE.registerNamer(MULTI_EPISODE_NAMER);
			NAMING_SERVICE.registerNamer(MOVIE_NAMER);
			NAMING_SERVICE.registerNamer(SUBTITLE_NAMER);
			NAMING_SERVICE.registerNamer(MEDIA_RELEASE_NAMER);
			NAMING_SERVICE.registerNamer(SUBTITLE_RELEASE_NAMER);
		}
		catch (IntrospectionException e)
		{
			e.printStackTrace();
		}
	}

	private NamingStandards()
	{
		// utility class
	}
}
