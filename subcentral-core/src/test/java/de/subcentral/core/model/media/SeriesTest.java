package de.subcentral.core.model.media;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class SeriesTest
{
	@Test
	public void testCreateSeasonsToEpisodesMap()
	{
		Series psych = new Series("Psych");
		Season season1 = psych.addSeason(1);
		Season season2 = psych.addSeason(2);
		Episode epi1x1 = psych.addEpisode(season1, 1, "Pilot");
		Episode epiSpecial = psych.addEpisode("Special episode");

		Map<Season, List<Episode>> expectedSeasonsAndEpisodes = new LinkedHashMap<>(3);
		expectedSeasonsAndEpisodes.put(season1, ImmutableList.of(epi1x1));
		expectedSeasonsAndEpisodes.put(season2, ImmutableList.of());
		expectedSeasonsAndEpisodes.put(null, ImmutableList.of(epiSpecial));

		Map<Season, List<Episode>> seasonsAndEpisodes = psych.createSeasonsToEpisodesMap();
		assertEquals(expectedSeasonsAndEpisodes, seasonsAndEpisodes);
	}
}
