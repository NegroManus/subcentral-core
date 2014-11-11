package de.subcentral.core.model.media;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class EpisodeOfSeriesMediaFilter implements Predicate<List<Media>>
{
	private final Series	series;

	public EpisodeOfSeriesMediaFilter(Series series)
	{
		this.series = Objects.requireNonNull(series, "series");
	}

	public Series getSeries()
	{
		return series;
	}

	@Override
	public boolean test(List<Media> media)
	{
		for (Media m : media)
		{
			if (m instanceof Episode && series.equals(((Episode) m).getSeries()))
			{
				continue;
			}
			return false;
		}
		return true;
	}
}
