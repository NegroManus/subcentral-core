package de.subcentral.core.metadata.media;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
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
public class MultiEpisodeHelper {
	private final List<Episode> episodes;

	public MultiEpisodeHelper() {
		this(2);
	}

	public MultiEpisodeHelper(int initialCapacity) {
		this.episodes = new ArrayList<>(initialCapacity);
	}

	public MultiEpisodeHelper(Collection<? extends Episode> c) {
		this.episodes = new ArrayList<>(c);
	}

	public MultiEpisodeHelper(Episode... episodes) {
		this.episodes = new ArrayList<>(Arrays.asList(episodes));
	}

	/**
	 * Determines whether the object to test is a multi-episode. Returns true if {@code obj} is an {@link Iterable} of more than one {@link Episode}. Otherwise returns false.
	 * 
	 * @param obj
	 *            the object to test
	 * @return whether {@code obj} is a multi-episode
	 */
	public static boolean isMultiEpisode(Object obj) {
		if (obj instanceof Iterable) {
			int size = 0;
			for (Object o : (Iterable<?>) obj) {
				if (o instanceof Episode) {
					size++;
					continue;
				}
				return false;
			}
			return size > 1;
		}
		return false;
	}

	public static MultiEpisodeHelper of(Object episodes) throws IllegalArgumentException {
		if (episodes instanceof Iterable) {
			MultiEpisodeHelper me = null;
			for (Object o : (Iterable<?>) episodes) {
				if (o instanceof Episode) {
					if (me == null) {
						me = new MultiEpisodeHelper(2);
					}
					me.episodes.add((Episode) o);
				}
				else {
					return null;
				}
			}
			return me;
		}
		return null;
	}

	public static MultiEpisodeHelper of(Iterable<? super Episode> episodes) throws IllegalArgumentException {
		return of(episodes, 2);
	}

	public static MultiEpisodeHelper of(Collection<? super Episode> episodes) throws IllegalArgumentException {
		return of(episodes, episodes.size());
	}

	private static MultiEpisodeHelper of(Iterable<? super Episode> episodes, int expectedSize) throws IllegalArgumentException {
		MultiEpisodeHelper me = new MultiEpisodeHelper(expectedSize);
		for (Object e : episodes) {
			if (e instanceof Episode) {
				me.episodes.add((Episode) e);
			}
			else {
				throw new IllegalArgumentException("episode iterable can only contain Episodes but contains " + episodes);
			}
		}
		return me;
	}

	public List<Episode> getEpisodes() {
		return episodes;
	}

	public void setEpisodes(List<Episode> episodes) {
		this.episodes.clear();
		this.episodes.addAll(episodes);
	}

	public Series getCommonSeries() {
		if (episodes.isEmpty()) {
			return null;
		}
		Series series = episodes.get(0).getSeries();
		for (int i = 1; i < episodes.size(); i++) {
			if (!series.equals(episodes.get(i).getSeries())) {
				return null;
			}
		}
		return series;
	}

	public Set<Season> getSeasons() {
		Set<Season> seasons = new HashSet<>(1);
		for (Episode epi : episodes) {
			Season season = epi.getSeason();
			if (season != null) {
				seasons.add(season);
			}
		}
		return seasons;
	}

	public Season getCommonSeason() {
		if (episodes.isEmpty()) {
			return null;
		}
		Season season = episodes.get(0).getSeason();
		if (season == null) {
			return null;
		}
		for (int i = 1; i < episodes.size(); i++) {
			if (!season.equals(episodes.get(i).getSeason())) {
				return null;
			}
		}
		return season;
	}

	public boolean allNumberedInSeries() {
		if (episodes.isEmpty()) {
			return false;
		}
		for (Episode epi : episodes) {
			if (!epi.isNumberedInSeries()) {
				return false;
			}
		}
		return true;
	}

	public boolean allNumberedInSeason() {
		if (episodes.isEmpty()) {
			return false;
		}
		for (Episode epi : episodes) {
			if (!epi.isNumberedInSeason()) {
				return false;
			}
		}
		return true;
	}

	public List<Integer> getNumbersInSeries() {
		List<Integer> nums = new ArrayList<>(episodes.size());
		for (Episode epi : episodes) {
			Integer numInSeries = epi.getNumberInSeries();
			if (numInSeries != null) {
				nums.add(numInSeries);
			}
		}
		return nums;
	}

	public List<Integer> getNumbersInSeason() {
		List<Integer> nums = new ArrayList<>(episodes.size());
		for (Episode epi : episodes) {
			Integer numInSeason = epi.getNumberInSeason();
			if (numInSeason != null) {
				nums.add(numInSeason);
			}
		}
		return nums;
	}

	public List<String> getTitles() {
		List<String> titles = new ArrayList<>(episodes.size());
		for (Episode epi : episodes) {
			String title = epi.getTitle();
			if (title != null) {
				titles.add(title);
			}
		}
		return titles;
	}

	public ListMultimap<Series, Episode> orderedBySeries() {
		ArrayListMultimap<Series, Episode> multimap = ArrayListMultimap.create();
		for (Episode epi : episodes) {
			multimap.put(epi.getSeries(), epi);
		}
		return multimap;
	}

	public ListMultimap<Season, Episode> orderedBySeason() {
		ArrayListMultimap<Season, Episode> multimap = ArrayListMultimap.create();
		for (Episode epi : episodes) {
			multimap.put(epi.getSeason(), epi);
		}
		return multimap;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof MultiEpisodeHelper) {
			return episodes.equals(((MultiEpisodeHelper) obj).episodes);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 3).append(episodes).toHashCode();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(MultiEpisodeHelper.class).add("episodes", episodes).toString();
	}

	public static List<List<Integer>> splitIntoConsecutiveRanges(List<Integer> nums) {
		if (nums.isEmpty()) {
			return ImmutableList.of();
		}
		List<List<Integer>> ranges = new ArrayList<>(1);
		Integer previous = nums.get(0);
		Integer current;
		List<Integer> range = new ArrayList<>();
		range.add(previous);
		for (int i = 1; i < nums.size(); i++) {
			current = nums.get(i);
			if (current.intValue() - 1 == previous.intValue()) {
				range.add(current);
			}
			else {
				ranges.add(range);
				range = new ArrayList<>();
				range.add(current);
			}
			previous = current;
		}
		ranges.add(range);
		return ranges;
	}
}
