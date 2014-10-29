package de.subcentral.core.naming;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.RegularAvMedia;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;

public class NamingTest
{
	private static final String	MOVIE_NAME			= "The Lord of the Rings: The Fellowship of the Ring";
	private static final String	MOVIE_REL_NAME		= "The.Lord.of.the.Rings.The.Fellowship.of.the.Ring.2001.EXTENDED.PL.1080p.BluRay.X264-AiHD";
	private static final String	MOVIE_SUB_NAME		= "The Lord of the Rings: The Fellowship of the Ring de";
	private static final String	MOVIE_SUB_REL_NAME	= "The.Lord.of.the.Rings.The.Fellowship.of.the.Ring.2001.EXTENDED.PL.1080p.BluRay.X264-AiHD.de-SubCentral";

	@BeforeClass
	public static void beforeClass()
	{
		NamingService ns = NamingStandards.getDefaultNamingService();
		System.out.println(ns);
	}

	@Before
	public void before()
	{
		NamingService ns = NamingStandards.getDefaultNamingService();
		System.out.println(ns);
	}

	@Test
	public void testMovieNaming()
	{
		RegularAvMedia movie = new RegularAvMedia(MOVIE_NAME);
		movie.setDate(Year.of(2001));
		String name = NamingStandards.getDefaultMediaNamer().name(movie);
		System.out.println(name);
		Assert.assertEquals(MOVIE_NAME, name);
	}

	@Test
	public void testMediaReleaseNaming()
	{
		RegularAvMedia movie = new RegularAvMedia(MOVIE_NAME);
		movie.setDate(Year.of(2001));
		Release rel = Release.create(MOVIE_REL_NAME, movie, "AiHD", "EXTENDED", "PL", "1080p", "BluRay", "X264");
		String name = NamingStandards.getDefaultReleaseNamer().name(rel, ImmutableMap.of(MediaNamer.PARAM_INCLUDE_YEAR_KEY, Boolean.TRUE));
		System.out.println(name);
		Assert.assertEquals(MOVIE_REL_NAME, name);
		Assert.assertEquals(MOVIE_REL_NAME,
				NamingStandards.getDefaultNamingService().name(rel, ImmutableMap.of(MediaNamer.PARAM_INCLUDE_YEAR_KEY, Boolean.TRUE)));
	}

	@Test
	public void testSubtitleNaming()
	{
		RegularAvMedia movie = new RegularAvMedia(MOVIE_NAME);
		movie.setDate(Year.of(2001));
		Subtitle sub = new Subtitle(movie);
		sub.setLanguage("de");
		String name = NamingStandards.getDefaultSubtitleNamer().name(sub);
		System.out.println(name);
		Assert.assertEquals(MOVIE_SUB_NAME, name);
		Assert.assertEquals(MOVIE_SUB_NAME, NamingStandards.getDefaultNamingService().name(sub));
	}

	@Test
	public void testSubtitleReleaseNaming()
	{
		RegularAvMedia movie = new RegularAvMedia(MOVIE_NAME);
		movie.setDate(Year.of(2001));

		Release mediaRel = Release.create(MOVIE_REL_NAME, movie, "AiHD", "EXTENDED", "PL", "1080p", "BluRay", "X264");
		SubtitleAdjustment rel = SubtitleAdjustment.create(mediaRel, "de", "SubCentral");
		String name = NamingStandards.getDefaultSubtitleAdjustmentNamer().name(rel, ImmutableMap.of(MediaNamer.PARAM_INCLUDE_YEAR_KEY, Boolean.TRUE));
		System.out.println(name);
		Assert.assertEquals(MOVIE_SUB_REL_NAME, name);
		Assert.assertEquals(MOVIE_SUB_REL_NAME,
				NamingStandards.getDefaultNamingService().name(rel, ImmutableMap.of(MediaNamer.PARAM_INCLUDE_YEAR_KEY, Boolean.TRUE)));
	}

	/**
	 * Checks all possible combinations if series, season and episode are included.
	 */
	@Test
	public void testSeasonedEpisodeNamingSeriesSeasonEpisode()
	{
		List<String> expectedNames = new ArrayList<>();
		expectedNames.add("Psych");
		expectedNames.add("Psych Pilot");
		expectedNames.add("Psych E01");
		expectedNames.add("Psych E01");
		expectedNames.add("Psych E01 Pilot");
		expectedNames.add("Psych");
		expectedNames.add("Psych Pilot");
		expectedNames.add("Psych E01");
		expectedNames.add("Psych E01");
		expectedNames.add("Psych E01 Pilot");
		expectedNames.add("Psych Webisodes");
		expectedNames.add("Psych Webisodes Pilot");
		expectedNames.add("Psych Webisodes E01");
		expectedNames.add("Psych Webisodes E01");
		expectedNames.add("Psych Webisodes E01 Pilot");
		expectedNames.add("Psych S01");
		expectedNames.add("Psych S01 Pilot");
		expectedNames.add("Psych S01E01");
		expectedNames.add("Psych S01E01");
		expectedNames.add("Psych S01E01 Pilot");
		expectedNames.add("Psych S01");
		expectedNames.add("Psych S01 Webisodes");
		expectedNames.add("Psych S01 Pilot");
		expectedNames.add("Psych S01 Webisodes Pilot");
		expectedNames.add("Psych S01E01");
		expectedNames.add("Psych S01 Webisodes E01");
		expectedNames.add("Psych S01E01");
		expectedNames.add("Psych S01E01 Pilot");
		expectedNames.add("Psych S01 Webisodes E01");
		expectedNames.add("Psych S01 Webisodes E01 Pilot");
		SeasonedEpisodeNamer epiNamer = (SeasonedEpisodeNamer) NamingStandards.getDefaultSeasonedEpisodeNamer();
		assertSeasonedEpisodeNamingSeriesSeasonEpisode(epiNamer, expectedNames);
	}

	public void assertSeasonedEpisodeNamingSeriesSeasonEpisode(SeasonedEpisodeNamer namer, List<String> expectedNames)
	{ // series, season, episode

		List<String> names = new ArrayList<>();

		// series Exx
		// series epititle
		// series epinum
		// series epinum epititle
		Series series = new Series("Psych");
		Season season = new Season(series);
		Episode epi = new Episode(series);
		names.add(namer.name(epi));

		epi.setTitle("Pilot");
		names.add(namer.name(epi));

		epi.setNumberInSeries(1);
		epi.setTitle(null);
		names.add(namer.name(epi));

		epi.setTitle("Pilot");
		names.add(namer.name(epi));
		names.add(namer.name(epi, ImmutableMap.of(SeasonedEpisodeNamer.PARAM_ALWAYS_INCLUDE_EPISODE_TITLE_KEY, Boolean.TRUE)));

		// series Sxx Exx
		// series Sxx epititle
		// series Sxx epinum
		// series Sxx epinum epititle
		epi.setSeason(season);
		epi.setNumberInSeries(null);
		epi.setTitle(null);
		names.add(namer.name(epi));

		epi.setTitle("Pilot");
		names.add(namer.name(epi));

		epi.setNumberInSeason(1);
		epi.setTitle(null);
		names.add(namer.name(epi));

		epi.setTitle("Pilot");
		names.add(namer.name(epi));
		names.add(namer.name(epi, ImmutableMap.of(SeasonedEpisodeNamer.PARAM_ALWAYS_INCLUDE_EPISODE_TITLE_KEY, Boolean.TRUE)));

		// series seasontitle Exx
		// series seasontitle epititle
		// series seasontitle epinum
		// series seasontitle epinum epititle
		season.setTitle("Webisodes");
		epi.setNumberInSeason(null);
		epi.setTitle(null);
		names.add(namer.name(epi));

		epi.setTitle("Pilot");
		names.add(namer.name(epi));

		epi.setNumberInSeason(1);
		epi.setTitle(null);
		names.add(namer.name(epi));

		epi.setTitle("Pilot");
		names.add(namer.name(epi));
		names.add(namer.name(epi, ImmutableMap.of(SeasonedEpisodeNamer.PARAM_ALWAYS_INCLUDE_EPISODE_TITLE_KEY, Boolean.TRUE)));

		// series seasonnum Exx
		// series seasonnum epititle
		// series {seasonnum epinum}
		// series {seasonnum epinum} epititle
		season.setNumber(1);
		season.setTitle(null);
		epi.setNumberInSeason(null);
		epi.setTitle(null);
		names.add(namer.name(epi));

		epi.setTitle("Pilot");
		names.add(namer.name(epi));

		epi.setNumberInSeason(1);
		epi.setTitle(null);
		names.add(namer.name(epi));

		epi.setTitle("Pilot");
		names.add(namer.name(epi));
		names.add(namer.name(epi, ImmutableMap.of(SeasonedEpisodeNamer.PARAM_ALWAYS_INCLUDE_EPISODE_TITLE_KEY, Boolean.TRUE)));

		// series seasonnum seasontitle Exx
		// series seasonnum seasontitle epititle
		// series seasonnum seasontitle epinum
		// series seasonnum seasontitle epinum epititle
		season.setTitle("Webisodes");
		epi.setNumberInSeason(null);
		epi.setTitle(null);
		names.add(namer.name(epi));
		names.add(namer.name(epi, ImmutableMap.of(SeasonedEpisodeNamer.PARAM_ALWAYS_INCLUDE_SEASON_TITLE_KEY, Boolean.TRUE)));

		epi.setTitle("Pilot");
		names.add(namer.name(epi));
		names.add(namer.name(epi, ImmutableMap.of(SeasonedEpisodeNamer.PARAM_ALWAYS_INCLUDE_SEASON_TITLE_KEY, Boolean.TRUE)));

		epi.setNumberInSeason(1);
		epi.setTitle(null);
		names.add(namer.name(epi));
		names.add(namer.name(epi, ImmutableMap.of(SeasonedEpisodeNamer.PARAM_ALWAYS_INCLUDE_SEASON_TITLE_KEY, Boolean.TRUE)));

		epi.setTitle("Pilot");
		names.add(namer.name(epi));
		names.add(namer.name(epi, ImmutableMap.of(SeasonedEpisodeNamer.PARAM_ALWAYS_INCLUDE_EPISODE_TITLE_KEY, Boolean.TRUE)));
		names.add(namer.name(epi, ImmutableMap.of(SeasonedEpisodeNamer.PARAM_ALWAYS_INCLUDE_SEASON_TITLE_KEY, Boolean.TRUE)));
		names.add(namer.name(epi, ImmutableMap.of(SeasonedEpisodeNamer.PARAM_ALWAYS_INCLUDE_SEASON_TITLE_KEY,
				Boolean.TRUE,
				SeasonedEpisodeNamer.PARAM_ALWAYS_INCLUDE_EPISODE_TITLE_KEY,
				Boolean.TRUE)));

		for (String name : names)
		{
			System.out.println(name);
		}
		for (int i = 0; i < expectedNames.size(); i++)
		{
			Assert.assertEquals(expectedNames.get(i), names.get(i));
		}
	}
}
