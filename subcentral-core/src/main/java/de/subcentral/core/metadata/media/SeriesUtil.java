package de.subcentral.core.metadata.media;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.naming.EpisodeNamer;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NamingUtil;

public class SeriesUtil
{
	public List<String> generateNamesIncludingSeriesAliases(Episode epi, NamingService namingService, Map<String, Object> parameters)
	{
		Series series = epi.getSeries();
		if (series == null || series.getAliasNames().isEmpty())
		{
			return ImmutableList.of(namingService.name(epi, parameters));
		}
		List<Map<String, Object>> parametersList = new ArrayList<>(1 + series.getAliasNames().size());
		parametersList.add(parameters);
		for (String alias : series.getAliasNames())
		{
			Map<String, Object> aliasParams = new HashMap<>(parameters);
			aliasParams.put(EpisodeNamer.PARAM_SERIES_NAME, alias);
			parametersList.add(aliasParams);
		}
		return NamingUtil.generateNames(epi, namingService, parametersList);
	}

	// Seasons and Episode
	public List<Episode> filterEpisodesBySeason(List<Episode> episodes, Season season)
	{
		ImmutableList.Builder<Episode> episInSeason = ImmutableList.builder();
		for (Episode epi : episodes)
		{
			if (Objects.equals(season, epi.getSeason()))
			{
				episInSeason.add(epi);
			}
		}
		return episInSeason.build();
	}

	/**
	 * Returns an immutable map of all the seasons of this series mapped to their corresponding episodes, plus {@code null} mapped to the episodes which are not part of a season.<br/>
	 * The map contains a {@code null} key which value is a list of all the episodes with no season, this list may be empty but it is never {@code null}. The map may contain a mapping between a season
	 * and an empty list for seasons without episodes.<br/>
	 * The seasons (the keys) are in the same order as in the {@link #getSeasons() seasons list}. The episodes for each season are in the same order as in the {@link #getEpisodes() episodes list}.
	 * 
	 * @return an immutable map with the seasons as keys (including the {@code null} season) and their episodes as values
	 * @throws IllegalStateException
	 *             if the season of an episode in the {@link #getEpisodes() episodes list} is not contained in the {@link #getSeasons() seasons list}
	 */
	public static Map<Season, List<Episode>> buildSeasonsToEpisodesMap(List<Season> seasons, List<Episode> episodes) throws IllegalStateException
	{
		// preserve insertion order -> use a LinkedHashMap
		Map<Season, List<Episode>> seasonsAndEpis = new LinkedHashMap<>(seasons.size());
		for (Season s : seasons)
		{
			seasonsAndEpis.put(s, new ArrayList<>());
		}
		// for the Episodes without a Season
		seasonsAndEpis.put(null, new ArrayList<>());
		for (Episode epi : episodes)
		{
			List<Episode> seasonEpiList = seasonsAndEpis.get(epi.getSeason());
			if (seasonEpiList == null)
			{
				throw new IllegalStateException("The seasons list of this series does not contain the season of the following episode: " + epi);
			}
			seasonEpiList.add(epi);
		}
		return Collections.unmodifiableMap(seasonsAndEpis);
	}

	/**
	 * @return the air date ({@link Episode#getDate()}) of the first episode of this series or <code>null</code> if this series has no episodes
	 */
	public static Temporal getDateOfFirstEpisode(List<Episode> episodes)
	{
		return episodes.isEmpty() ? null : episodes.get(0).getDate();
	}

	/**
	 * @return the air date ({@link Episode#getDate()}) of the last episode of this series or <code>null</code> if this series has no episodes
	 */
	public static Temporal getDateOfLastEpisode(List<Episode> episodes)
	{
		return episodes.isEmpty() ? null : episodes.get(episodes.size() - 1).getDate();
	}

	private SeriesUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
