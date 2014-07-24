package de.subcentral.core.naming;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Movie;
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

	@Test
	public void testMovieNaming()
	{
		Movie movie = new Movie(MOVIE_NAME, Year.of(2001));
		String name = NamingStandards.MOVIE_NAMER.name(movie);
		System.out.println(name);
		Assert.assertEquals(MOVIE_NAME, name);
	}

	@Test
	public void testMediaReleaseNaming()
	{
		Release rel = Release.create(MOVIE_REL_NAME, new Movie(MOVIE_NAME, Year.of(2001)), "AiHD", "EXTENDED", "PL", "1080p", "BluRay", "X264");
		String name = NamingStandards.RELEASE_NAMER.name(rel,
				NamingStandards.NAMING_SERVICE,
				ImmutableMap.of(MovieNamer.PARAM_INCLUDE_YEAR_KEY, Boolean.TRUE));
		System.out.println(name);
		Assert.assertEquals(MOVIE_REL_NAME, name);
		Assert.assertEquals(MOVIE_REL_NAME,
				NamingStandards.NAMING_SERVICE.name(rel, ImmutableMap.of(MovieNamer.PARAM_INCLUDE_YEAR_KEY, Boolean.TRUE)));
	}

	@Test
	public void testSubtitleNaming()
	{
		Subtitle sub = new Subtitle(new Movie(MOVIE_NAME, Year.of(2001)));
		sub.setLanguage("de");
		String name = NamingStandards.SUBTITLE_NAMER.name(sub, NamingStandards.NAMING_SERVICE);
		System.out.println(name);
		Assert.assertEquals(MOVIE_SUB_NAME, name);
		Assert.assertEquals(MOVIE_SUB_NAME, NamingStandards.NAMING_SERVICE.name(sub));
	}

	@Test
	public void testSubtitleReleaseNaming()
	{
		Release mediaRel = Release.create(MOVIE_REL_NAME, new Movie(MOVIE_NAME, Year.of(2001)), "AiHD", "EXTENDED", "PL", "1080p", "BluRay", "X264");
		SubtitleAdjustment rel = SubtitleAdjustment.create(mediaRel, "de", "SubCentral");
		String name = NamingStandards.SUBTITLE_ADJUSTMENT_NAMER.name(rel,
				NamingStandards.NAMING_SERVICE,
				ImmutableMap.of(MovieNamer.PARAM_INCLUDE_YEAR_KEY, Boolean.TRUE));
		System.out.println(name);
		Assert.assertEquals(MOVIE_SUB_REL_NAME, name);
		Assert.assertEquals(MOVIE_SUB_REL_NAME,
				NamingStandards.NAMING_SERVICE.name(rel, ImmutableMap.of(MovieNamer.PARAM_INCLUDE_YEAR_KEY, Boolean.TRUE)));
	}

	/**
	 * Checks all possible combinations if series, season and episode are included.
	 */
	@Test
	public void testSeasonedEpisodeNamingSeriesSeasonEpisode()
	{
		List<String> expectedNames = new ArrayList<>();
		expectedNames.add("Psych Exx");
		expectedNames.add("Psych Pilot");
		expectedNames.add("Psych E01");
		expectedNames.add("Psych E01");
		expectedNames.add("Psych E01 Pilot");
		expectedNames.add("Psych Sxx Exx");
		expectedNames.add("Psych Sxx Pilot");
		expectedNames.add("Psych Sxx E01");
		expectedNames.add("Psych Sxx E01");
		expectedNames.add("Psych Sxx E01 Pilot");
		expectedNames.add("Psych Webisodes Exx");
		expectedNames.add("Psych Webisodes Pilot");
		expectedNames.add("Psych Webisodes E01");
		expectedNames.add("Psych Webisodes E01");
		expectedNames.add("Psych Webisodes E01 Pilot");
		expectedNames.add("Psych S01 Exx");
		expectedNames.add("Psych S01 Pilot");
		expectedNames.add("Psych S01E01");
		expectedNames.add("Psych S01E01");
		expectedNames.add("Psych S01E01 Pilot");
		expectedNames.add("Psych S01 Exx");
		expectedNames.add("Psych S01 Webisodes Exx");
		expectedNames.add("Psych S01 Pilot");
		expectedNames.add("Psych S01 Webisodes Pilot");
		expectedNames.add("Psych S01E01");
		expectedNames.add("Psych S01 Webisodes E01");
		expectedNames.add("Psych S01E01");
		expectedNames.add("Psych S01E01 Pilot");
		expectedNames.add("Psych S01 Webisodes E01");
		expectedNames.add("Psych S01 Webisodes E01 Pilot");
		assertSeasonedEpisodeNamingSeriesSeasonEpisode(NamingStandards.SEASONED_EPISODE_NAMER, expectedNames);
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
		namer.setAlwaysIncludeEpisodeTitle(true);
		names.add(namer.name(epi));
		namer.setAlwaysIncludeEpisodeTitle(false);

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
		namer.setAlwaysIncludeEpisodeTitle(true);
		names.add(namer.name(epi));
		namer.setAlwaysIncludeEpisodeTitle(false);

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
		namer.setAlwaysIncludeEpisodeTitle(true);
		names.add(namer.name(epi));
		namer.setAlwaysIncludeEpisodeTitle(false);

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
		namer.setAlwaysIncludeEpisodeTitle(true);
		names.add(namer.name(epi));
		namer.setAlwaysIncludeEpisodeTitle(false);

		// series seasonnum seasontitle Exx
		// series seasonnum seasontitle epititle
		// series seasonnum seasontitle epinum
		// series seasonnum seasontitle epinum epititle
		season.setTitle("Webisodes");
		epi.setNumberInSeason(null);
		epi.setTitle(null);
		names.add(namer.name(epi));
		namer.setAlwaysIncludeSeasonTitle(true);
		names.add(namer.name(epi));
		namer.setAlwaysIncludeSeasonTitle(false);

		epi.setTitle("Pilot");
		names.add(namer.name(epi));
		namer.setAlwaysIncludeSeasonTitle(true);
		names.add(namer.name(epi));
		namer.setAlwaysIncludeSeasonTitle(false);

		epi.setNumberInSeason(1);
		epi.setTitle(null);
		names.add(namer.name(epi));
		namer.setAlwaysIncludeSeasonTitle(true);
		names.add(namer.name(epi));
		namer.setAlwaysIncludeSeasonTitle(false);

		epi.setTitle("Pilot");
		names.add(namer.name(epi));
		namer.setAlwaysIncludeEpisodeTitle(true);
		names.add(namer.name(epi));
		namer.setAlwaysIncludeEpisodeTitle(false);
		namer.setAlwaysIncludeSeasonTitle(true);
		names.add(namer.name(epi));
		namer.setAlwaysIncludeEpisodeTitle(true);
		names.add(namer.name(epi));
		namer.setAlwaysIncludeEpisodeTitle(false);
		namer.setAlwaysIncludeSeasonTitle(false);

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
