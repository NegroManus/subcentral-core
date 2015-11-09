package de.subcentral.core.parsing;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.metadata.media.GenericMedia;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleFile;

public class ParsingDefaults
{
	/**
	 * Pattern for media names like "The Lord of the Rings (2003)", "The Office (UK)".<br/>
	 * Groups
	 * <ol>
	 * <li>name</li>
	 * <li>title (may be equal to name)</li>
	 * <li>year (or null)</li>
	 * <li>country code (or null)</li>
	 * </ol>
	 */
	public static final String PATTERN_MEDIA_NAME = "((.*?)(?:\\s+\\((?:(\\d{4})|(\\p{Upper}{2}))\\))?)";

	private static final EpisodeMapper				EPISODE_MAPPER						= new EpisodeMapper();
	private static final Mapper<List<Episode>>		SINGLETON_LIST_EPISODE_MAPPER		= createSingletonListMapper(EPISODE_MAPPER);
	private static final MultiEpisodeMapper			MULTI_EPISODE_MAPPER				= new MultiEpisodeMapper(EPISODE_MAPPER);
	private static final MovieMapper				MOVIE_MAPPER						= new MovieMapper();
	private static final Mapper<List<Movie>>		SINGLETON_LIST_MOVIE_MAPPER			= createSingletonListMapper(MOVIE_MAPPER);
	private static final GenericMediaMapper			GENERIC_MEDIA_MAPPER					= new GenericMediaMapper();
	private static final Mapper<List<GenericMedia>>	SINGLETON_LIST_GENERIC_MEDIA_MAPPER	= createSingletonListMapper(GENERIC_MEDIA_MAPPER);
	private static final ReleaseMapper				RELEASE_MAPPER						= new ReleaseMapper();
	private static final SubtitleMapper				SUBTITLE_MAPPER						= new SubtitleMapper();
	private static final SubtitleAdjustmentMapper	SUBTITLE_ADJUSTMENT_MAPPER			= new SubtitleAdjustmentMapper();

	private static final <E> Mapper<List<E>> createSingletonListMapper(Mapper<E> elementMapper)
	{
		return (props, pps) -> ImmutableList.of(elementMapper.map(props, pps));
	}

	public static Mapper<Episode> getDefaultEpisodeMapper()
	{
		return EPISODE_MAPPER;
	}

	public static Mapper<List<Episode>> getDefaultSingletonListEpisodeMapper()
	{
		return SINGLETON_LIST_EPISODE_MAPPER;
	}

	public static Mapper<List<Episode>> getDefaultMultiEpisodeMapper()
	{
		return MULTI_EPISODE_MAPPER;
	}

	public static final Mapper<Movie> getDefaultMovieMapper()
	{
		return MOVIE_MAPPER;
	}

	public static Mapper<List<Movie>> getDefaultSingletonListMovieMapper()
	{
		return SINGLETON_LIST_MOVIE_MAPPER;
	}

	public static GenericMediaMapper getDefaultGenericMediaMapper()
	{
		return GENERIC_MEDIA_MAPPER;
	}

	public static Mapper<List<GenericMedia>> getDefaultSingletonListGenericMediaMapper()
	{
		return SINGLETON_LIST_GENERIC_MEDIA_MAPPER;
	}

	public static final Mapper<Release> getDefaultReleaseMapper()
	{
		return RELEASE_MAPPER;
	}

	public static final Mapper<Subtitle> getDefaultSubtitleMapper()
	{
		return SUBTITLE_MAPPER;
	}

	public static final Mapper<SubtitleFile> getDefaultSubtitleAdjustmentMapper()
	{
		return SUBTITLE_ADJUSTMENT_MAPPER;
	}

	private ParsingDefaults()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
