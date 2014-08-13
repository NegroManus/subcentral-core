package de.subcentral.core.parsing;

import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.Map;
import java.util.Objects;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.util.SimplePropDescriptor;

public class EpisodeMapper extends AbstractMapper<Episode>
{
	@Override
	public Episode doMap(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService)
	{
		Series series = new Series();
		series.setType(propParsingService.parse(props, Series.PROP_TYPE, String.class));
		String name = props.get(Series.PROP_NAME);
		String title = props.get(Series.PROP_TITLE);
		series.setName(name);
		if (!Objects.equals(name, title))
		{
			series.setTitle(title);
		}
		series.setDate(propParsingService.parse(props, Series.PROP_DATE, Temporal.class));
		series.setCountriesOfOrigin(propParsingService.parseList(props, Series.PROP_COUNTRIES_OF_ORIGIN, String.class));

		Episode epi = series.newEpisode();
		epi.setNumberInSeries(propParsingService.parse(props, Episode.PROP_NUMBER_IN_SERIES, Integer.class));
		epi.setNumberInSeason(propParsingService.parse(props, Episode.PROP_NUMBER_IN_SEASON, Integer.class));
		epi.setTitle(props.get(Episode.PROP_TITLE));
		epi.setDate(propParsingService.parse(props, Episode.PROP_DATE, LocalDate.class));

		if (props.containsKey(Season.PROP_NUMBER))
		{
			Season season = series.newSeason();
			season.setNumber(propParsingService.parse(props, Season.PROP_NUMBER, Integer.class));
			epi.setSeason(season);
		}

		return epi;
	}
}
