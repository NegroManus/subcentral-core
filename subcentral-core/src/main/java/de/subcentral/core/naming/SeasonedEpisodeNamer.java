package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;

/**
 * Possible naming combinations:
 * <p>
 * <b>Series, Season, Episode</b>
 * <ul>
 * <li>series seasonnum seasontitle epinum epititle</li>
 * <li>series seasonnum seasontitle epinum</li>
 * <li>series seasonnum seasontitle epititle</li>
 * <li>series seasonnum seasontitle Exx</li>
 * <li>series {seasonnum epinum} epititle</li>
 * <li>series {seasonnum epinum}</li>
 * <li>series seasonnum epititle</li>
 * <li>series seasonnum Exx</li>
 * <li>series seasontitle epinum epititle</li>
 * <li>series seasontitle epinum</li>
 * <li>series seasontitle epititle</li>
 * <li>series seasontitle Exx</li>
 * <li>series Sxx epinum epititle</li>
 * <li>series Sxx epinum</li>
 * <li>series Sxx epititle</li>
 * <li>series Sxx Exx</li>
 * <li>series epinum epititle</li>
 * <li>series epinum</li>
 * <li>series epititle</li>
 * <li>series Exx</li>
 * </ul>
 * 
 * <b>Season, Episode</b>
 * <ul>
 * <li>seasonnum seasontitle epinum epititle</li>
 * <li>seasonnum seasontitle epinum</li>
 * <li>seasonnum seasontitle epititle</li>
 * <li>seasonnum seasontitle Exx</li>
 * <li>{seasonnum epinum} epititle</li>
 * <li>{seasonnum epinum}</li>
 * <li>seasonnum epititle</li>
 * <li>seasonnum Exx</li>
 * <li>seasontitle epinum epititle</li>
 * <li>seasontitle epinum</li>
 * <li>seasontitle epititle</li>
 * <li>seasontitle Exx</li>
 * <li>Sxx epinum epititle</li>
 * <li>Sxx epinum</li>
 * <li>Sxx epititle</li>
 * <li>Sxx Exx</li>
 * <li>epinum epititle</li>
 * <li>epinum</li>
 * <li>epititle</li>
 * <li>Exx</li>
 * </ul>
 * 
 * <b>Episode</b>
 * <ul>
 * <li>epinum</li>
 * <li>epinum epititle</li>
 * <li>epititle</li>
 * <li>Exx</li>
 * </ul>
 * </p>
 * 
 * The following separators are needed:
 * 
 * <pre>
 * series-seasonnum x
 * series-seasontitle x
 * series-sxx x
 * series-epinum x
 * series-epititle x
 * series-exx x
 * -> series-anything (all)
 * 
 * seasonnum-seasontitle
 * seasonnum-epinum
 * seasonnum-epititle
 * seasonnum-exx
 * -> seasonnum-seasontitle
 * -> seasonnum-epinum
 * -> season-epi
 * -> seasonnum-epinum (duplicate)
 *  
 * seasontitle-epinum
 * seasontitle-epititle
 * seasontitle-exx
 * -> season-epi (all, duplicate)
 * 
 * epinum-epititle
 * -> epinum-epititle
 * </pre>
 * 
 * 
 * @author mhertram
 *
 */
public class SeasonedEpisodeNamer extends AbstractPropertySequenceNamer<Episode>
{
	/**
	 * The parameter key for the Boolean value "includeSeries". The default value is {@code true}.
	 */
	public static final String	PARAM_INCLUDE_SERIES_KEY	= "includeSeries";

	/**
	 * The parameter key for the Boolean value "includeSeason". The default value is {@code true}.
	 */
	public static final String	PARAM_INCLUDE_SEASON_KEY	= "includeSeason";

	private boolean				alwaysIncludeSeasonTitle	= false;
	private boolean				alwaysIncludeEpisodeTitle	= false;

	private String				undefinedSeriesPlaceholder	= "UNNAMED_SERIES";
	private String				undefinedSeasonPlaceholder	= "Sxx";
	private String				undefinedEpisodePlaceholder	= "Exx";

	public SeasonedEpisodeNamer()
	{

	}

	// booleans
	public boolean getAlwaysIncludeSeasonTitle()
	{
		return alwaysIncludeSeasonTitle;
	}

	public void setAlwaysIncludeSeasonTitle(boolean alwaysIncludeSeasonTitle)
	{
		this.alwaysIncludeSeasonTitle = alwaysIncludeSeasonTitle;
	}

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

	public String getUndefinedSeasonPlaceholder()
	{
		return undefinedSeasonPlaceholder;
	}

	public void setUndefinedSeasonPlaceholder(String undefinedSeasonPlaceholder)
	{
		this.undefinedSeasonPlaceholder = undefinedSeasonPlaceholder;
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
	public String doName(Episode epi, Map<String, Object> params)
	{
		// settings
		boolean includeSeries = Namings.readParameter(params, PARAM_INCLUDE_SERIES_KEY, Boolean.class, Boolean.TRUE);
		boolean includeSeason = Namings.readParameter(params, PARAM_INCLUDE_SEASON_KEY, Boolean.class, Boolean.TRUE);

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

		// add season
		if (includeSeason && epi.isPartOfSeason())
		{
			Season season = epi.getSeason();
			if (!season.isNumbered() && !season.isTitled())
			{
				b.appendString(Season.PROP_TITLE, undefinedSeasonPlaceholder);
			}
			else
			{
				b.appendIf(Season.PROP_NUMBER, season.getNumber(), season.isNumbered());
				b.appendIf(Season.PROP_TITLE, season.getTitle(), alwaysIncludeSeasonTitle || !season.isNumbered());
			}
		}

		// add episode
		if (!epi.isNumberedInSeries() && !epi.isNumberedInSeason() && !epi.isTitled())
		{
			b.appendString(Episode.PROP_TITLE, undefinedEpisodePlaceholder);
		}
		else
		{
			if (epi.isPartOfSeason())
			{
				b.appendIf(Episode.PROP_NUMBER_IN_SEASON, epi.getNumberInSeason(), epi.isNumberedInSeason());
				b.appendIf(Episode.PROP_TITLE, epi.getTitle(), alwaysIncludeEpisodeTitle || !epi.isNumberedInSeason());
			}
			else
			{
				b.appendIf(Episode.PROP_NUMBER_IN_SERIES, epi.getNumberInSeries(), epi.isNumberedInSeries());
				b.appendIf(Episode.PROP_TITLE, epi.getTitle(), alwaysIncludeEpisodeTitle || !epi.isNumberedInSeries());
			}
		}

		return b.build();
	}
}
