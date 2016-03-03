package de.subcentral.core.metadata.media;

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
 * <code>MultiEpisodeHelper</code> does not implement {@link MediaCollection} because it is just a helper class and not an "official", releasable collection of <code>Media</code>. In contrast to
 * <code>Media</code> collections like <code>Series</code> and <code>Season</code>, a collection of multiple episodes has no properties of its own. Therefore it is not a valid <code>Media</code>
 * instance. <br/>
 * If multiple <code>Episodes</code> are released in a single <code>Release</code>, they are released as a set of materials and not as one material (contrary to whole <code>Series</code> or
 * <code>Seasons</code> which are released as one material).
 * </p>
 *
 */
public class MultiEpisodeHelper extends ArrayList<Episode>
{
	private static final long serialVersionUID = 870081295286403057L;

	/**
	 * Determines whether the object to test is a multi-episode. Returns true if {@code obj} is an {@link Iterable} of more than one {@link Episode}. Otherwise returns false.
	 * 
	 * @param obj
	 *            the object to test
	 * @return whether {@code obj} is a multi-episode
	 */
	public static boolean isMultiEpisode(Object obj)
	{
		if (obj instanceof Iterable)
		{
			int size = 0;
			for (Object o : (Iterable<?>) obj)
			{
				if (o instanceof Episode)
				{
					size++;
					continue;
				}
				return false;
			}
			return size > 1;
		}
		return false;
	}

	public static MultiEpisodeHelper of(Object episodes) throws IllegalArgumentException
	{
		if (episodes instanceof Iterable)
		{
			List<Episode> epis = null;
			for (Object o : (Iterable<?>) episodes)
			{
				if (o instanceof Episode)
				{
					if (epis == null)
					{
						epis = new ArrayList<>(2);
					}
					epis.add((Episode) o);
				}
				else
				{
					return null;
				}
			}
			if (epis != null)
			{
				return new MultiEpisodeHelper(epis);
			}
		}
		return null;
	}

	public static MultiEpisodeHelper of(Iterable<? super Episode> episodes) throws IllegalArgumentException
	{
		return of(episodes, 2);
	}

	public static MultiEpisodeHelper of(Collection<? super Episode> episodes) throws IllegalArgumentException
	{
		return of(episodes, episodes.size());
	}

	private static MultiEpisodeHelper of(Iterable<? super Episode> episodes, int expectedSize) throws IllegalArgumentException
	{
		MultiEpisodeHelper me = new MultiEpisodeHelper(expectedSize);
		for (Object e : episodes)
		{
			if (e instanceof Episode)
			{
				me.add((Episode) e);
			}
			else
			{
				throw new IllegalArgumentException("episode iterable can only contain Episodes but contains " + episodes);
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
		Integer current;
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

	public boolean allNumberedInSeries()
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

	public boolean allNumberedInSeason()
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

	public ListMultimap<Series, Episode> orderedBySeries()
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

	public ListMultimap<Season, Episode> orderedBySeason()
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
