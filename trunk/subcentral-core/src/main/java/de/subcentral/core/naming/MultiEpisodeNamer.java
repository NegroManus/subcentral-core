package de.subcentral.core.naming;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.MultiEpisodeHelper;
import de.subcentral.core.util.SimplePropDescriptor;

public class MultiEpisodeNamer extends AbstractPropertySequenceNamer<Collection<Episode>>
{
	public static final String		SEPARATION_TYPE_ADDITION	= "addition";
	public static final String		SEPARATION_TYPE_RANGE		= "range";

	private SeasonedEpisodeNamer	seasonedEpiNamer			= (SeasonedEpisodeNamer) NamingStandards.getDefaultSeasonedEpisodeNamer();
	private Namer<Episode>			episodeNamer				= NamingStandards.getDefaultEpisodeNamer();

	@Override
	public void buildName(PropSequenceNameBuilder b, Collection<Episode> episodes, Map<String, Object> namingSettings) throws NamingException
	{
		if (episodes.isEmpty())
		{
			return;
		}
		MultiEpisodeHelper me = new MultiEpisodeHelper(episodes);

		String firstEpiName = episodeNamer.name(me.get(0), ImmutableMap.of());
		b.appendString(MultiEpisodeHelper.PROP_EPISODES, firstEpiName);

		if (me.getCommonSeries() != null)
		{
			if (me.getCommonSeason() != null)
			{
				if (me.areAllNumberedInSeason())
				{
					b.overwriteLastProperty(Episode.PROP_NUMBER_IN_SEASON);
					List<List<Integer>> numberRanges = MultiEpisodeHelper.splitIntoConsecutiveRanges(me.getNumbersInSeason());
					// append first
					appendRange(b, numberRanges.get(0), false, true);

					// append others; i=1 !
					for (int i = 1; i < numberRanges.size(); i++)
					{
						appendRange(b, numberRanges.get(i), false, false);
					}
				}
				else
				{
					for (int i = 1; i < me.size(); i++)
					{
						seasonedEpiNamer.buildName(b, me.get(i), ImmutableMap.of("includeSeries", Boolean.FALSE, "includeSeason", Boolean.FALSE));
					}
				}
			}
			else if (me.getSeasons().isEmpty())
			{
				// no seasons at all
				if (me.areAllNumberedInSeries())
				{
					b.overwriteLastProperty(Episode.PROP_NUMBER_IN_SERIES);
					List<List<Integer>> numberRanges = MultiEpisodeHelper.splitIntoConsecutiveRanges(me.getNumbersInSeries());
					// append first
					appendRange(b, numberRanges.get(0), true, true);

					// append others; i=1 !
					for (int i = 1; i < numberRanges.size(); i++)
					{
						appendRange(b, numberRanges.get(i), true, false);
					}
				}
			}
			else
			{
				// different seasons
				for (int i = 1; i < me.size(); i++)
				{
					seasonedEpiNamer.buildName(b, me.get(i), ImmutableMap.of("includeSeries", Boolean.FALSE));
				}
			}
		}
		else
		{
			// no common series
			for (int i = 1; i < me.size(); i++)
			{
				String epiName = episodeNamer.name(me.get(i), ImmutableMap.of());
				b.appendString(MultiEpisodeHelper.PROP_EPISODES, epiName);
			}
		}
	}

	private void appendRange(PropSequenceNameBuilder builder, List<Integer> range, boolean numberInSeries, boolean omitFirstNumber)
	{
		SimplePropDescriptor prop = numberInSeries ? Episode.PROP_NUMBER_IN_SERIES : Episode.PROP_NUMBER_IN_SEASON;
		switch (range.size())
		{
			case 0:
				break;
			case 1:
				if (!omitFirstNumber)
				{
					builder.append(prop, range.get(0), SEPARATION_TYPE_ADDITION);
				}
				break;
			case 2:
				if (!omitFirstNumber)
				{
					builder.append(prop, range.get(0), SEPARATION_TYPE_ADDITION);
				}
				builder.append(prop, range.get(1), SEPARATION_TYPE_ADDITION);
				break;
			default:
				if (!omitFirstNumber)
				{
					builder.append(prop, range.get(0), SEPARATION_TYPE_RANGE);
				}
				builder.append(prop, range.get(range.size() - 1), SEPARATION_TYPE_RANGE);
				break;
		}
	}
}
