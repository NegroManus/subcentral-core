package de.subcentral.core.naming;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.MultiEpisodeHelper;
import de.subcentral.core.util.SimplePropDescriptor;

public class MultiEpisodeNamer extends AbstractPropertySequenceNamer<List<? extends Episode>>
{
	public static final String					PROP_NAME_EPISODES			= "episodes";
	public static final SimplePropDescriptor	PROP_EPISODES				= new SimplePropDescriptor(MultiEpisodeHelper.class, PROP_NAME_EPISODES);

	public static final String					SEPARATION_TYPE_ADDITION	= "addition";
	public static final String					SEPARATION_TYPE_RANGE		= "range";

	private final EpisodeNamer					episodeNamer;

	public MultiEpisodeNamer(PropSequenceNameBuilder.Config config)
	{
		this(config, null);
	}

	public MultiEpisodeNamer(PropSequenceNameBuilder.Config config, EpisodeNamer episodeNamer)
	{
		super(config);
		this.episodeNamer = episodeNamer != null ? episodeNamer : new EpisodeNamer(config);
	}

	public EpisodeNamer getEpisodeNamer()
	{
		return episodeNamer;
	}

	@Override
	protected void appendName(PropSequenceNameBuilder b, List<? extends Episode> episodes, Map<String, Object> params) throws NamingException
	{
		if (episodes.isEmpty())
		{
			return;
		}
		MultiEpisodeHelper me = new MultiEpisodeHelper(episodes);
		Episode firstEpi = me.get(0);
		episodeNamer.appendName(b, firstEpi, params);

		if (me.getCommonSeries() != null)
		{
			if (me.getCommonSeason() != null)
			{
				if (me.allNumberedInSeason())
				{
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
						episodeNamer.appendOwnName(b, epi, params);
					}
				}
			}
			else if (me.getSeasons().isEmpty())
			{
				// no seasons at all
				if (me.allNumberedInSeries())
				{
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
						episodeNamer.appendOwnName(b, epi, params);
					}
				}
			}
			else
			{
				// different seasons
				for (int i = 1; i < me.size(); i++)
				{
					Episode epi = me.get(i);
					episodeNamer.appendOwnName(b, epi, params);
				}
			}
		}
		else
		{
			// no common series
			for (int i = 1; i < me.size(); i++)
			{
				Episode epi = me.get(i);
				String epiName = episodeNamer.name(epi, ImmutableMap.of());
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
					builder.append(prop, range.get(0), SEPARATION_TYPE_ADDITION);
				}
				builder.append(prop, range.get(range.size() - 1), SEPARATION_TYPE_RANGE);
				break;
		}
	}
}
