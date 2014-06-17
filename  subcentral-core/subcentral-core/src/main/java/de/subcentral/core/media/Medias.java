package de.subcentral.core.media;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Joiner;

import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NoNamerRegisteredException;
import de.subcentral.core.util.Settings;

public class Medias
{
	public static final Comparator<Media>	MEDIA_NAME_COMPARATOR	= new MediaNameComparator();

	static final class MediaNameComparator implements Comparator<Media>
	{
		@Override
		public int compare(Media o1, Media o2)
		{
			if (o1 == null)
			{
				return o2 == null ? 0 : 1;
			}
			if (o2 == null)
			{
				return -1;
			}
			return Settings.STRING_ORDERING.compare(o1.getNameOrCompute(), o2.getNameOrCompute());
		}
	}

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

	public static Episode newSeasonedEpisode(String seriesName, int seasonNumber, int episodeNumber)
	{
		return newSeasonedEpisode(seriesName, null, seasonNumber, null, episodeNumber, null);
	}

	public static Episode newSeasonedEpisode(String seriesName, int seasonNumber, int episodeNumber, String episodeTitle)
	{
		return newSeasonedEpisode(seriesName, null, seasonNumber, null, episodeNumber, episodeTitle);
	}

	public static Episode newSeasonedEpisode(String seriesName, String seasonTitle, int episodeNumber, String episodeTitle)
	{
		return newSeasonedEpisode(seriesName, null, Media.UNNUMBERED, seasonTitle, episodeNumber, episodeTitle);
	}

	public static Episode newSeasonedEpisode(String seriesName, String seriesTitle, int seasonNumber, String seasonTitle, int episodeNumber,
			String episodeTitle)
	{
		if (seriesName == null)
		{
			throw new IllegalArgumentException("Series name must be set");
		}
		Series series = new Series();
		series.setType(Series.TYPE_SEASONED);
		series.setName(seriesName);
		series.setTitle(seriesTitle);
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

	public static Episode newMiniSeriesEpisode(String seriesName, int episodeNumber)
	{
		return newMiniSeriesEpisode(seriesName, null, episodeNumber, null);
	}

	public static Episode newMiniSeriesEpisode(String seriesName, int episodeNumber, String episodeTitle)
	{
		return newMiniSeriesEpisode(seriesName, null, episodeNumber, episodeTitle);
	}

	public static Episode newMiniSeriesEpisode(String seriesName, String seriesTitle, int episodeNumber, String episodeTitle)
	{
		if (seriesName == null)
		{
			throw new IllegalArgumentException("Series name must be set");
		}
		Series series = new Series();
		series.setType(Series.TYPE_MINI_SERIES);
		series.setName(seriesName);
		series.setTitle(seriesTitle);
		Episode epi = series.addEpisode();
		epi.setNumberInSeries(episodeNumber);
		epi.setTitle(episodeTitle);
		return epi;
	}

	public static Episode newDatedEpisode(String seriesName, Temporal date)
	{
		return newDatedEpisode(seriesName, null, date, null);
	}

	public static Episode newDatedEpisode(String seriesName, Temporal date, String episodeTitle)
	{
		return newDatedEpisode(seriesName, null, date, episodeTitle);
	}

	public static Episode newDatedEpisode(String seriesName, String seriesTitle, Temporal date, String episodeTitle)
	{
		if (seriesName == null)
		{
			throw new IllegalArgumentException("Series name must be set");
		}
		Series series = new Series();
		series.setType(Series.TYPE_DATED);
		series.setName(seriesName);
		series.setTitle(seriesTitle);
		Episode epi = series.addEpisode();
		epi.setDate(date);
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
