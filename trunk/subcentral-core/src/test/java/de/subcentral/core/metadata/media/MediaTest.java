package de.subcentral.core.metadata.media;

import java.util.ArrayList;
import java.util.List;

import de.subcentral.core.Settings;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.util.TimeUtil;

public class MediaTest
{
	public static void main(String[] args)
	{
		for (int i = 0; i < 1000; i++)
		{
			List<Episode> media = new ArrayList<>();
			media.add(Episode.createSeasonedEpisode("Psych", 2, 1));
			media.add(Episode.createSeasonedEpisode("Psych", 2, 2));
			media.add(Episode.createSeasonedEpisode("Psych", "Webisodes", 1, "The Golden Phantom"));
			media.add(Episode.createSeasonedEpisode("Psych", 1, 3));
			media.add(Episode.createSeasonedEpisode("How I Met Your Mother", 2, 1));
			media.add(Episode.createSeasonedEpisode("How I Met Your Mother", 3, 1));

			long start = System.nanoTime();
			media.sort(Settings.createDefaultOrdering());
			System.out.println(TimeUtil.durationMillis(start, System.nanoTime()));

			media.forEach(s -> System.out.println(NamingDefaults.getDefaultSeasonedEpisodeNamer().name(s)));
			System.out.println(TimeUtil.durationMillis(start, System.nanoTime()));
		}
	}
}