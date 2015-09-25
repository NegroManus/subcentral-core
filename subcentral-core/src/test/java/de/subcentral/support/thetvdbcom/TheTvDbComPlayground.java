package de.subcentral.support.thetvdbcom;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.naming.EpisodeNamer;
import de.subcentral.core.naming.NamingDefaults;

public class TheTvDbComPlayground
{
	/**
	 * -Dhttp.proxyHost=10.151.249.76 -Dhttp.proxyPort=8080
	 * 
	 * @param args
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IllegalArgumentException, IOException
	{
		// A3ACA9D28A27792D

		TheTvDbComMetadataDb db = new TheTvDbComMetadataDb();
		db.setApiKey("A3ACA9D28A27792D");
		List<Series> results = db.searchByObject(new Series("lost"), Series.class);
		System.out.println("Search results:");
		results.stream().forEach((Object obj) -> System.out.println(obj));
		System.out.println();

		Series series = db.get(results.get(0).getIds().get(TheTvDbCom.SOURCE_ID), Series.class);
		System.out.println("Series:");
		System.out.println(series);
		System.out.println();
		System.out.println("Seasons:");
		series.getSeasons().stream().forEach((Season sns) ->
		{
			System.out.println(NamingDefaults.getDefaultSeasonNamer().name(sns));
			// System.out.println(sns);
		});
		System.out.println();
		System.out.println("Episodes:");
		series.getEpisodes().stream().forEach((Episode epi) ->
		{
			System.out.println((epi.isSpecial() ? "[s] " : "[-] ") + NamingDefaults.getDefaultEpisodeNamer().name(epi, ImmutableMap.of(EpisodeNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.TRUE)));
			// System.out.println(epi);
		});
	}
}