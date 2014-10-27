package de.subcentral.core.naming;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.MultiEpisodeHelper;
import de.subcentral.core.util.SimplePropDescriptor;

public class MultiEpisodeNamer extends AbstractPropertySequenceNamer<Collection<Episode>>
{
	public static final String									PROP_NAME_EPISODES			= "episodes";
	public static final SimplePropDescriptor					PROP_EPISODES				= new SimplePropDescriptor(MultiEpisodeHelper.class,
																									PROP_NAME_EPISODES);

	public static final String									SEPARATION_TYPE_ADDITION	= "addition";
	public static final String									SEPARATION_TYPE_RANGE		= "range";

	private Map<String, AbstractPropertySequenceNamer<Episode>>	seriesTypeNamers			= new HashMap<>(3);
	private AbstractPropertySequenceNamer<Episode>				defaultNamer				= (AbstractPropertySequenceNamer<Episode>) NamingStandards.getDefaultSeasonedEpisodeNamer();

	public Map<String, AbstractPropertySequenceNamer<Episode>> getSeriesTypeNamers()
	{
		return seriesTypeNamers;
	}

	public AbstractPropertySequenceNamer<Episode> registerSeriesTypeNamer(String seriesType, AbstractPropertySequenceNamer<Episode> namer)
	{
		return seriesTypeNamers.put(seriesType, namer);
	}

	public AbstractPropertySequenceNamer<Episode> unregisterSeriesTypeNamer(String seriesType)
	{
		return seriesTypeNamers.remove(seriesType);
	}

	public Namer<Episode> getDefaultNamer()
	{
		return defaultNamer;
	}

	public void setDefaultNamer(AbstractPropertySequenceNamer<Episode> defaultNamer)
	{
		Validate.notNull(defaultNamer, "defaultNamer");
		this.defaultNamer = defaultNamer;
	}

	public AbstractPropertySequenceNamer<Episode> getNamer(Episode epi)
	{
		if (epi.getSeries() != null && epi.getSeries().getType() != null)
		{
			return seriesTypeNamers.getOrDefault(epi.getSeries().getType(), defaultNamer);
		}
		else
		{
			return defaultNamer;
		}
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Collection<Episode> episodes, Map<String, Object> namingSettings) throws NamingException
	{
		if (episodes.isEmpty())
		{
			return;
		}
		MultiEpisodeHelper me = new MultiEpisodeHelper(episodes);
		Episode firstEpi = me.get(0);
		String firstEpiName = getNamer(firstEpi).name(firstEpi, ImmutableMap.of());
		b.appendString(PROP_EPISODES, firstEpiName);

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
						Episode epi = me.get(i);
						getNamer(epi).buildName(b, epi, ImmutableMap.of("includeSeries", Boolean.FALSE, "includeSeason", Boolean.FALSE));
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
				else
				{
					for (int i = 1; i < me.size(); i++)
					{
						Episode epi = me.get(i);
						getNamer(epi).buildName(b, epi, ImmutableMap.of("includeSeries", Boolean.FALSE));
					}
				}
			}
			else
			{
				// different seasons
				for (int i = 1; i < me.size(); i++)
				{
					Episode epi = me.get(i);
					getNamer(epi).buildName(b, epi, ImmutableMap.of("includeSeries", Boolean.FALSE));
				}
			}
		}
		else
		{
			// no common series
			for (int i = 1; i < me.size(); i++)
			{
				Episode epi = me.get(i);
				getNamer(epi);
				String epiName = getNamer(epi).name(epi, ImmutableMap.of());
				b.appendString(PROP_EPISODES, epiName);
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
