package de.subcentral.core.parse;

import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.Map;
import java.util.Objects;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.util.SimplePropDescriptor;

public class EpisodeMapper extends AbstractMapper<Episode>
{
	public EpisodeMapper()
	{
		// default constructor
	}

	public EpisodeMapper(ParsePropService parsePropService)
	{
		super(parsePropService);
	}

	@Override
	public Episode map(Map<SimplePropDescriptor, String> props)
	{
		Series series = new Series();
		series.setType(parsePropService.parse(props, Series.PROP_TYPE, String.class));
		String name = props.get(Series.PROP_NAME);
		String title = props.get(Series.PROP_TITLE);
		series.setName(name);
		if (!Objects.equals(name, title))
		{
			series.setTitle(title);
		}
		series.setDate(parsePropService.parse(props, Series.PROP_DATE, Temporal.class));
		series.setCountries(parsePropService.parseList(props, Series.PROP_COUNTRIES, String.class));

		Episode epi = new Episode(series);
		epi.setNumberInSeries(parsePropService.parse(props, Episode.PROP_NUMBER_IN_SERIES, Integer.class));
		epi.setNumberInSeason(parsePropService.parse(props, Episode.PROP_NUMBER_IN_SEASON, Integer.class));
		epi.setTitle(props.get(Episode.PROP_TITLE));
		epi.setDate(parsePropService.parse(props, Episode.PROP_DATE, LocalDate.class));

		if (props.containsKey(Season.PROP_NUMBER))
		{
			Season season = new Season(series);
			season.setNumber(parsePropService.parse(props, Season.PROP_NUMBER, Integer.class));
			epi.setSeason(season);
		}

		return epi;
	}

	@Override
	protected Class<?> getTargetType()
	{
		return Episode.class;
	}
}
