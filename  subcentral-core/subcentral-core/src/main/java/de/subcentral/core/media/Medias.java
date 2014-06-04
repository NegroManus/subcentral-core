package de.subcentral.core.media;

import java.util.List;

public class Medias
{
	public static String getNameOfMultiEpisodes(List<Episode> episodes)
	{
		// TODO
		return null;
	}

	public static Episode newEpisode(String seriesTitle, int seasonNumber, int episodeNumber)
	{
		return newEpisode(seriesTitle, null, seasonNumber, null, episodeNumber, null);
	}

	public static Episode newEpisode(String seriesTitle, int seasonNumber, int episodeNumber, String episodeTitle)
	{
		return newEpisode(seriesTitle, null, seasonNumber, null, episodeNumber, episodeTitle);
	}

	public static Episode newEpisode(String seriesTitle, String seasonTitle, int episodeNumber, String episodeTitle)
	{
		return newEpisode(seriesTitle, null, Media.UNNUMBERED, seasonTitle, episodeNumber, episodeTitle);
	}

	public static Episode newEpisode(String seriesTitle, String seriesName, int seasonNumber, String seasonTitle, int episodeNumber,
			String episodeTitle)
	{
		Series series = new Series();
		series.setTitle(seriesTitle);
		series.setExplicitName(seriesName);
		Season season = null;
		if (seasonNumber != Media.UNNUMBERED || seasonTitle != null)
		{
			season = series.addSeason();
			season.setNumber(seasonNumber);
			season.setTitle(seasonTitle);
		}
		Episode epi = series.addEpisode(season);
		epi.setNumberInSeason(episodeNumber);
		epi.setTitle(episodeTitle);
		return epi;
	}

	private Medias()
	{
		// utility class
	}
}
