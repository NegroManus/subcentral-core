package de.subcentral.core.media;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NoNamerRegisteredException;

public class Medias
{
	public static MultiEpisode newMultiEpisode(List<? extends Media> media)
	{
		MultiEpisode me = new MultiEpisode(media.size());
		for (Media m : media)
		{
			if (m instanceof Episode)
			{
				me.add((Episode) m);
			}
			return null;
		}
		return me;
	}

	public static String name(List<? extends Media> media, NamingService namingService, String mediaSeparator)
	{
		int numOfMedia = media.size();
		if (numOfMedia == 0)
		{
			return "";
		}
		else if (numOfMedia == 1)
		{
			return namingService.name(media.get(0));
		}
		else
		{
			try
			{
				MultiEpisode me = MultiEpisode.of(media);
				return namingService.name(me);
			}
			catch (IllegalArgumentException | NoNamerRegisteredException e)
			{
				List<String> names = new ArrayList<>(media.size());
				for (Media m : media)
				{
					names.add(namingService.name(m));
				}
				return Joiner.on(mediaSeparator).join(names);
			}
		}
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
		if (seriesTitle == null && seriesName == null)
		{
			throw new IllegalArgumentException("Series title or name must be set");
		}
		Series series = new Series();
		series.setTitle(seriesTitle);
		series.setName(seriesName);
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

	public static Movie newMovie(String title)
	{
		return newMovie(title, null);
	}

	public static Movie newMovie(String title, String name)
	{
		Movie movie = new Movie();
		movie.setName(name);
		movie.setTitle(title);
		return movie;
	}

	private Medias()
	{
		// utility class
	}
}
