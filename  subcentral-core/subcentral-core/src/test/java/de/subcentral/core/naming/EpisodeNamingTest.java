package de.subcentral.core.naming;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;

public class EpisodeNamingTest
{

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
