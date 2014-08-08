package de.subcentral.core.parsing;

import java.time.temporal.Temporal;
import java.util.Map;
import java.util.Objects;

import de.subcentral.core.model.media.AvMediaItem;
import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.util.SimplePropDescriptor;

public class ReleaseParser extends AbstractPropertyParser<Release>
{

	public ReleaseParser(String domain)
	{
		super(domain);
	}

	@Override
	public Class<Release> getEntityType()
	{
		return Release.class;
	}

	@Override
	protected Release map(Map<SimplePropDescriptor, String> props)
	{
		// Media
		AvMediaItem mediaItem = null;
		// If episode info is contained
		if (props.containsKey(Series.PROP_NAME))
		{
			Series series = new Series();
			series.setType(pps.parse(props, Series.PROP_TYPE, String.class));
			String name = props.get(Series.PROP_NAME);
			String title = props.get(Series.PROP_TITLE);
			series.setName(name);
			if (!Objects.equals(name, title))
			{
				series.setTitle(title);
			}
			series.setDate(pps.parse(props, Series.PROP_DATE, Temporal.class));
			series.setCountriesOfOrigin(pps.parseList(props, Series.PROP_COUNTRIES_OF_ORIGIN, String.class));

			Episode epi = series.newEpisode();
			epi.setNumberInSeries(pps.parse(props, Episode.PROP_NUMBER_IN_SERIES, Integer.class));
			epi.setNumberInSeason(pps.parse(props, Episode.PROP_NUMBER_IN_SEASON, Integer.class));
			epi.setTitle(props.get(Episode.PROP_TITLE));
			epi.setDate(pps.parse(props, Episode.PROP_DATE, Temporal.class));

			if (props.containsKey(Season.PROP_NUMBER))
			{
				Season season = series.newSeason();
				season.setNumber(pps.parse(props, Season.PROP_NUMBER, Integer.class));
				epi.setSeason(season);
			}

			mediaItem = epi;
		}
		// If movie info is contained
		else if (props.containsKey(Movie.PROP_NAME))
		{
			Movie mov = new Movie();
			mov.setName(props.get(Movie.PROP_NAME));
			mov.setTitle(props.get(Movie.PROP_TITLE));
			mov.setDate(pps.parse(props, Movie.PROP_DATE, Temporal.class));
			mediaItem = mov;
		}

		// Release
		Release rls = new Release();
		rls.getMedia().add(mediaItem);
		rls.setGroup(pps.parse(props, Release.PROP_GROUP, Group.class));
		rls.getTags().addAll(pps.parseList(props, Release.PROP_TAGS, Tag.class));
		rls.setSource(props.get(Release.PROP_SOURCE));
		rls.setSourceUrl(props.get(Release.PROP_SOURCE_URL));

		Releases.normalizeTags(rls);

		return rls;
	}

}
