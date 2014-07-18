package de.subcentral.core.naming;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.MultiEpisodeHelper;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.util.SimplePropDescriptor;

public class MultiEpisodeNamer extends AbstractPropertySequenceNamer<MultiEpisodeHelper>
{
	public static final String		SEPARATION_TYPE_ADDITION	= "addition";
	public static final String		SEPARATION_TYPE_RANGE		= "range";

	private SeasonedEpisodeNamer	episodeNamer				= NamingStandards.SEASONED_EPISODE_NAMER;

	@Override
	public Class<MultiEpisodeHelper> getType()
	{
		return MultiEpisodeHelper.class;
	}

	@Override
	public String doName(MultiEpisodeHelper me, NamingService namingService, Map<String, Object> namingSettings) throws NamingException
	{
		if (me.isEmpty())
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(episodeNamer.name(me.get(0)));
		Series commonSeries = me.getCommonSeries();
		if (commonSeries != null)
		{
			Season commonSeason = me.getCommonSeason();
			if (commonSeason != null)
			{
				if (me.areAllNumberedInSeason())
				{
					List<List<Integer>> numberRanges = MultiEpisodeHelper.splitIntoConsecutiveRanges(me.getNumbersInSeason());
					List<Integer> firstRange = numberRanges.get(0);
					appendRange(sb, firstRange, false, true);

					for (int i = 1; i < numberRanges.size(); i++)
					{
						List<Integer> range = numberRanges.get(i);
						sb.append(getSeparatorBetween(Episode.PROP_NUMBER_IN_SEASON, Episode.PROP_NUMBER_IN_SEASON, SEPARATION_TYPE_ADDITION));
						appendRange(sb, range, false, false);
					}
				}
				else
				{
					for (int i = 1; i < me.size(); i++)
					{
						sb.append(getSeparatorBetween(MultiEpisodeHelper.PROP_EPISODES, MultiEpisodeHelper.PROP_EPISODES, SEPARATION_TYPE_ADDITION));
						sb.append(episodeNamer.name(me.get(i), ImmutableMap.of("includeSeries", Boolean.FALSE, "includeSeason", Boolean.FALSE)));
					}
				}
			}
			else if (me.getSeasons().isEmpty())
			{
				// no seasons at all
				if (me.areAllNumberedInSeries())
				{
					List<List<Integer>> numberRanges = MultiEpisodeHelper.splitIntoConsecutiveRanges(me.getNumbersInSeries());
					List<Integer> firstRange = numberRanges.get(0);
					appendRange(sb, firstRange, true, true);

					for (int i = 1; i < numberRanges.size(); i++)
					{
						List<Integer> range = numberRanges.get(i);
						sb.append(getSeparatorBetween(Episode.PROP_NUMBER_IN_SERIES, Episode.PROP_NUMBER_IN_SERIES, SEPARATION_TYPE_ADDITION));
						appendRange(sb, range, true, false);
					}
				}
			}
			else
			{
				// different seasons
				for (int i = 1; i < me.size(); i++)
				{
					sb.append(getSeparatorBetween(MultiEpisodeHelper.PROP_EPISODES, MultiEpisodeHelper.PROP_EPISODES, SEPARATION_TYPE_ADDITION));
					sb.append(episodeNamer.name(me.get(i), ImmutableMap.of("includeSeries", Boolean.FALSE, "includeSeason", Boolean.TRUE)));
				}
			}
		}
		else
		{
			// no common series
			for (int i = 1; i < me.size(); i++)
			{
				sb.append(getSeparatorBetween(MultiEpisodeHelper.PROP_EPISODES, MultiEpisodeHelper.PROP_EPISODES, SEPARATION_TYPE_ADDITION));
				sb.append(episodeNamer.name(me.get(i)));
			}
		}
		return sb.toString();
	}

	private void appendRange(StringBuilder sb, List<Integer> range, boolean numberInSeries, boolean omitFirstNumber)
	{
		SimplePropDescriptor prop = numberInSeries ? Episode.PROP_NUMBER_IN_SERIES : Episode.PROP_NUMBER_IN_SEASON;
		switch (range.size())
		{
			case 0:
				break;
			case 1:
				if (!omitFirstNumber)
				{
					sb.append(episodeNamer.propToString(prop, range.get(0), SEPARATION_TYPE_ADDITION));
				}
				break;
			case 2:
				if (!omitFirstNumber)
				{
					sb.append(episodeNamer.propToString(prop, range.get(0), SEPARATION_TYPE_ADDITION));
				}
				sb.append(getSeparatorBetween(prop, prop, SEPARATION_TYPE_ADDITION));
				sb.append(episodeNamer.propToString(prop, range.get(1), SEPARATION_TYPE_ADDITION));
				break;
			default:
				if (!omitFirstNumber)
				{
					sb.append(episodeNamer.propToString(prop, range.get(0), SEPARATION_TYPE_RANGE));
				}
				sb.append(getSeparatorBetween(prop, prop, SEPARATION_TYPE_RANGE));
				sb.append(episodeNamer.propToString(prop, range.get(range.size() - 1), SEPARATION_TYPE_RANGE));
				break;
		}
	}
}
