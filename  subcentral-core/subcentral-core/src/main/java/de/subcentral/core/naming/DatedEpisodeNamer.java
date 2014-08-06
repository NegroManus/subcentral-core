package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Series;

public class DatedEpisodeNamer extends AbstractPropertySequenceNamer<Episode>
{
	/**
	 * The parameter key for the Boolean value "includeSeries". The default value is {@code true}.
	 */
	public static final String	PARAM_INCLUDE_SERIES_KEY	= "includeSeries";

	private boolean				alwaysIncludeEpisodeTitle	= false;

	private String				undefinedSeriesPlaceholder	= "UNNAMED_SERIES";
	private String				undefinedEpisodePlaceholder	= "xxxx.xx.xx";

	public DatedEpisodeNamer()
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
	public Class<Episode> getEntityType()
	{
		return Episode.class;
	}

	@Override
	public String doName(Episode epi, Map<String, Object> params)
	{
		// settings
		boolean includeSeries = Namings.readParameter(params, PARAM_INCLUDE_SERIES_KEY, Boolean.class, Boolean.TRUE);

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
		if (epi.getDate() != null)
		{
			b.append(Episode.PROP_DATE, epi.getDate());
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
