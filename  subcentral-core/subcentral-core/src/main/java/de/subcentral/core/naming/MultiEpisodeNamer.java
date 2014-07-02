package de.subcentral.core.naming;

import java.util.List;

import de.subcentral.core.media.MultiEpisodeHelper;
import de.subcentral.core.media.Season;
import de.subcentral.core.media.Series;

public class MultiEpisodeNamer implements Namer<MultiEpisodeHelper>
{
	private SeasonedEpisodeNamer	episodeNamer					= NamingStandards.SEASONED_EPISODE_NAMER;

	private String					episodeNumberRangeSeparator		= "-";
	private String					episodeNumberAdditionSeparator	= "";
	private String					episodeAdditionSeparator		= " ";

	@Override
	public Class<MultiEpisodeHelper> getType()
	{
		return MultiEpisodeHelper.class;
	}

	@Override
	public String name(MultiEpisodeHelper me, NamingService namingService) throws NamingException
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
					appendRange(sb, firstRange, true);

					for (int i = 1; i < numberRanges.size(); i++)
					{
						List<Integer> range = numberRanges.get(i);
						sb.append(episodeNumberAdditionSeparator);
						appendRange(sb, range, false);
					}
				}
				else
				{
					for (int i = 1; i < me.size(); i++)
					{
						sb.append(episodeAdditionSeparator);
						sb.append(episodeNamer.name(me.get(i), false, false));
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
					appendRange(sb, firstRange, true);

					for (int i = 1; i < numberRanges.size(); i++)
					{
						List<Integer> range = numberRanges.get(i);
						sb.append(episodeNumberAdditionSeparator);
						appendRange(sb, range, false);
					}
				}
			}
			else
			{
				// different seasons
				for (int i = 1; i < me.size(); i++)
				{
					sb.append(episodeAdditionSeparator);
					sb.append(episodeNamer.name(me.get(i), false, true));
				}
			}
		}
		else
		{
			// no common series
			for (int i = 1; i < me.size(); i++)
			{
				sb.append(episodeAdditionSeparator);
				sb.append(episodeNamer.name(me.get(i)));
			}
		}
		return sb.toString();
	}

	private void appendRange(StringBuilder sb, List<Integer> range, boolean omitFirstNumber)
	{
		switch (range.size())
		{
			case 0:
				break;
			case 1:
				if (!omitFirstNumber)
				{
					sb.append(episodeNamer.formatEpisodeNumber(range.get(0)));
				}
				break;
			case 2:
				if (!omitFirstNumber)
				{
					sb.append(episodeNamer.formatEpisodeNumber(range.get(0)));
				}
				sb.append(episodeNumberAdditionSeparator);
				sb.append(episodeNamer.formatEpisodeNumber(range.get(1)));
				break;
			default:
				if (!omitFirstNumber)
				{
					sb.append(episodeNamer.formatEpisodeNumber(range.get(0)));
				}
				sb.append(episodeNumberRangeSeparator);
				sb.append(episodeNamer.formatEpisodeNumber(range.get(range.size() - 1)));
				break;
		}
	}
}
