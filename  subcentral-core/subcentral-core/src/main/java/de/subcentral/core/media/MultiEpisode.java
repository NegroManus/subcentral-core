package de.subcentral.core.media;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class MultiEpisode extends ArrayList<Episode>
{
	private static final long	serialVersionUID	= 870081295286403057L;

	public static MultiEpisode of(List<? extends Media> media) throws IllegalArgumentException
	{
		MultiEpisode me = new MultiEpisode(media.size());
		for (Media m : media)
		{
			if (m instanceof Episode)
			{
				me.add((Episode) m);
			}
			else
			{
				throw new IllegalArgumentException("media list can only contain Episodes but contains" + media);
			}
		}
		return me;
	}

	public static List<List<Integer>> splitIntoConsecutiveRanges(List<Integer> nums)
	{
		if (nums.isEmpty())
		{
			return ImmutableList.of();
		}
		List<List<Integer>> ranges = new ArrayList<>(1);
		Integer previous = nums.get(0);
		Integer current = null;
		List<Integer> range = new ArrayList<>();
		range.add(previous);
		for (int i = 1; i < nums.size(); i++)
		{
			current = nums.get(i);
			if (current.intValue() - 1 == previous.intValue())
			{
				range.add(current);
			}
			else
			{
				ranges.add(range);
				range = new ArrayList<>();
				range.add(current);
			}
			previous = current;
		}
		ranges.add(range);
		return ranges;
	}

	public MultiEpisode()
	{
		this(2);
	}

	public MultiEpisode(int initialCapacity)
	{
		super(initialCapacity);
	}

	public MultiEpisode(Collection<? extends Episode> c)
	{
		super(c);
	}

	public MultiEpisode(Episode... episodes)
	{
		super(Arrays.asList(episodes));
	}

	public Series getCommonSeries()
	{
		if (isEmpty())
		{
			return null;
		}
		Series series = get(0).getSeries();
		for (int i = 1; i < size(); i++)
		{
			if (!series.equals(get(i).getSeries()))
			{
				return null;
			}
		}
		return series;
	}

	public Set<Season> getSeasons()
	{
		if (isEmpty())
		{
			return ImmutableSet.of();
		}
		Set<Season> seasons = new HashSet<>(2);
		for (Episode epi : this)
		{
			Season season = epi.getSeason();
			if (season != null)
			{
				seasons.add(season);
			}
		}
		return seasons;
	}

	public Season getCommonSeason()
	{
		if (isEmpty())
		{
			return null;
		}
		Season season = get(0).getSeason();
		for (int i = 1; i < size(); i++)
		{
			if (!season.equals(get(i).getSeason()))
			{
				return null;
			}
		}
		return season;
	}

	public boolean getAllNumberedInSeries()
	{
		if (isEmpty())
		{
			return false;
		}
		for (Episode epi : this)
		{
			if (!epi.isNumberedInSeries())
			{
				return false;
			}
		}
		return true;
	}

	public boolean getAllNumberedInSeason()
	{
		if (isEmpty())
		{
			return false;
		}
		for (Episode epi : this)
		{
			if (!epi.isNumberedInSeason())
			{
				return false;
			}
		}
		return true;
	}

	public List<Integer> getNumbersInSeries()
	{
		if (isEmpty())
		{
			return ImmutableList.of();
		}
		List<Integer> nums = new ArrayList<>(size());
		for (Episode epi : this)
		{
			int numInSeries = epi.getNumberInSeries();
			if (numInSeries != Media.UNNUMBERED)
			{
				nums.add(numInSeries);
			}
		}
		return nums;
	}

	public List<Integer> getNumbersInSeason()
	{
		if (isEmpty())
		{
			return ImmutableList.of();
		}
		List<Integer> nums = new ArrayList<>(size());
		for (Episode epi : this)
		{
			int numInSeason = epi.getNumberInSeason();
			if (numInSeason != Media.UNNUMBERED)
			{
				nums.add(numInSeason);
			}
		}
		return nums;
	}

	public List<String> getTitles()
	{
		if (isEmpty())
		{
			return ImmutableList.of();
		}
		List<String> titles = new ArrayList<>(size());
		for (Episode epi : this)
		{
			String title = epi.getTitle();
			if (title != null)
			{
				titles.add(title);
			}
		}
		return titles;
	}

	@Override
	public String toString()
	{
		return "MultiEpisode" + super.toString();
	}
}
