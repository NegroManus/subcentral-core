package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Series;

public class MiniSeriesEpisodeNamer extends AbstractPropertySequenceNamer<Episode>
{
	/**
	 * The parameter key for the Boolean value "includeSeries".
	 */
	public static final String	PARAM_INCLUDE_SERIES_KEY		= "includeSeries";
	public static final Boolean	PARAM_INCLUDE_SERIES_DEFAULT	= Boolean.TRUE;

	private boolean				alwaysIncludeEpisodeTitle		= false;

	private String				undefinedSeriesPlaceholder		= "UNNAMED_SERIES";
	private String				undefinedEpisodePlaceholder		= "xx";

	public MiniSeriesEpisodeNamer()
	{

	}

	// booleans
	public boolean getAlwaysIncludeEpisodeTitle()
	{
		return alwaysIncludeEpisodeTitle;
	}

	public void setAlwaysIncludeEpisodeTitle(boolean alwaysIncludeEpisodeTitle)
	{
		this.alwaysIncludeEpisodeTitle = alwaysIncludeEpisodeTitle;
	}

	// placeholders
	public String getUndefinedSeriesPlaceholder()
	{
		return undefinedSeriesPlaceholder;
	}

	public void setUndefinedSeriesPlaceholder(String undefinedSeriesPlaceholder)
	{
		this.undefinedSeriesPlaceholder = undefinedSeriesPlaceholder;
	}

	public String getUndefinedEpisodePlaceholder()
	{
		return undefinedEpisodePlaceholder;
	}

	public void setUndefinedEpisodePlaceholder(String undefinedEpisodePlaceholder)
	{
		this.undefinedEpisodePlaceholder = undefinedEpisodePlaceholder;
	}

	@Override
	public Class<Episode> getType()
	{
		return Episode.class;
	}

	@Override
	public String doName(Episode epi, Map<String, Object> params)
	{
		// settings
		boolean includeSeries = Namings.readParameter(params, PARAM_INCLUDE_SERIES_KEY, Boolean.class, PARAM_INCLUDE_SERIES_DEFAULT);

		Builder b = new Builder();

		// add series
		if (includeSeries && epi.getSeries() != null)
		{
			Series series = epi.getSeries();
			if (series.getName() == null)
			{
				b.appendString(Episode.PROP_SERIES, undefinedSeriesPlaceholder);
			}
			else
			{
				b.appendString(Episode.PROP_SERIES, epi.getSeries().getName());
			}
		}

		// add episode
		if (epi.isNumberedInSeries())
		{
			b.append(Episode.PROP_NUMBER_IN_SERIES, epi.getNumberInSeries());
			if (alwaysIncludeEpisodeTitle)
			{
				b.append(Episode.PROP_TITLE, epi.getTitle());
			}
		}
		else
		{
			if (epi.isTitled())
			{
				b.append(Episode.PROP_TITLE, epi.getTitle());
			}
			else
			{
				b.appendString(Episode.PROP_TITLE, undefinedEpisodePlaceholder);
			}
		}

		return b.build();
	}
}
