package de.subcentral.core.model.media;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;

/**
 * A helper class for dealing with multiple episodes.
 * <p>
 * <code>MultiEpisodeHelper</code> does not implement {@link AvMediaCollection} because it is just a helper class and not an "official", releasable
 * collection of <code>Media</code>. In contrast to <code>Media</code> collections like <code>Series</code> and <code>Season</code>, a collection of
 * multiple episodes has no properties of its own. Therefore it is not a valid <code>Media</code> instance. <br/>
 * If multiple <code>Episodes</code> are released in a single <code>Release</code>, they are released as a set of materials and not as one material
 * (contrary to whole <code>Series</code> or <code>Seasons</code> which are released as one material).
 * </p>
 *
 */
public class MultiEpisodeHelper extends ArrayList<Episode>
{
	private static final long	serialVersionUID	= 870081295286403057L;

	public static boolean isMultiEpisode(Object obj)
	{
		if (obj != null && obj instanceof Collection)
		{
			for (Object o : (Collection<?>) obj)
			{
				if (o instanceof Episode)
				{
					continue;
				}
				return false;
			}
			return true;
		}
		return false;
	}

	public static MultiEpisodeHelper of(List<? extends Media> media) throws IllegalArgumentException
	{
		MultiEpisodeHelper me = new MultiEpisodeHelper(media.size());
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

	public MultiEpisodeHelper()
	{
		this(2);
	}

	public MultiEpisodeHelper(int initialCapacity)
	{
		super(initialCapacity);
	}

	public MultiEpisodeHelper(Collection<? extends Episode> c)
	{
		super(c);
	}

	public MultiEpisodeHelper(Episode... episodes)
	{
		super(Arrays.asList(episodes));
	}

	// to have getter and setter for the property "episodes"
	public List<Episode> getEpisodes()
	{
		return this;
	}

	public void setEpisodes(List<Episode> episodes)
	{
		clear();
		addAll(episodes);
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
		if (season == null)
		{
			return null;
		}
		for (int i = 1; i < size(); i++)
		{
			if (!season.equals(get(i).getSeason()))
			{
				return null;
			}
		}
		return season;
	}

	public boolean areAllNumberedInSeries()
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

	public boolean areAllNumberedInSeason()
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
			Integer numInSeries = epi.getNumberInSeries();
			if (numInSeries != null)
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
			Integer numInSeason = epi.getNumberInSeason();
			if (numInSeason != null)
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

	public ListMultimap<Series, Episode> splitBySeries()
	{
		if (isEmpty())
		{
			return ImmutableListMultimap.of();
		}
		ArrayListMultimap<Series, Episode> multimap = ArrayListMultimap.create();
		for (Episode epi : this)
		{
			multimap.put(epi.getSeries(), epi);
		}
		return multimap;
	}

	public ListMultimap<Season, Episode> splitBySeason()
	{
		if (isEmpty())
		{
			return ImmutableListMultimap.of();
		}
		ArrayListMultimap<Season, Episode> multimap = ArrayListMultimap.create();
		for (Episode epi : this)
		{
			multimap.put(epi.getSeason(), epi);
		}
		return multimap;
	}

	@Override
	public String toString()
	{
		return "MultiEpisode" + super.toString();
	}
}
