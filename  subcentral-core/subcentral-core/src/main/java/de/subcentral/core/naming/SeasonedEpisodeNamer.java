package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.util.Replacer;

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
public class SeasonedEpisodeNamer extends AbstractEpisodeNamer
{
	/**
	 * The naming setting key for the Boolean value "includeSeries".
	 */
	public static final String	PARAM_INCLUDE_SERIES_KEY				= "includeSeries";
	public static final Boolean	PARAM_INCLUDE_SERIES_DEFAULT			= Boolean.TRUE;

	/**
	 * The naming setting key for the Boolean value "includeSeason".
	 */
	public static final String	PARAM_INCLUDE_SEASON_KEY				= "includeSeason";
	public static final Boolean	PARAM_INCLUDE_SEASON_DEFAULT			= Boolean.TRUE;

	private boolean				alwaysIncludeSeasonTitle				= false;

	private String				seasonNumberFormat						= "S%02d";
	private Replacer			seasonTitleReplacer						= null;
	private String				seasonTitleFormat						= "%s";
	private String				episodeNumberFormat						= "E%02d";

	private String				seasonNumberAndTitleSeparator			= " ";
	private String				seasonNumberAndEpisodeNumberSeparator	= "";
	private String				seasonAndEpisodeSeparator				= " ";
	private String				episodeNumberAndTitleSeparator			= " ";

	private String				undefinedSeasonPlaceholder				= "Sxx";

	// booleans
	public boolean getAlwaysIncludeSeasonTitle()
	{
		return alwaysIncludeSeasonTitle;
	}

	public void setAlwaysIncludeSeasonTitle(boolean alwaysIncludeSeasonTitle)
	{
		this.alwaysIncludeSeasonTitle = alwaysIncludeSeasonTitle;
	}

	// formats and replacers
	public String getSeasonNumberFormat()
	{
		return seasonNumberFormat;
	}

	public void setSeasonNumberFormat(String seasonNumberFormat)
	{
		this.seasonNumberFormat = seasonNumberFormat;
	}

	public Replacer getSeasonTitleReplacer()
	{
		return seasonTitleReplacer;
	}

	public void setSeasonTitleReplacer(Replacer seasonTitleReplacer)
	{
		this.seasonTitleReplacer = seasonTitleReplacer;
	}

	public String getSeasonTitleFormat()
	{
		return seasonTitleFormat;
	}

	public void setSeasonTitleFormat(String seasonTitleFormat)
	{
		this.seasonTitleFormat = seasonTitleFormat;
	}

	public String getEpisodeNumberFormat()
	{
		return episodeNumberFormat;
	}

	public void setEpisodeNumberFormat(String episodeNumberFormat)
	{
		this.episodeNumberFormat = episodeNumberFormat;
	}

	// separators
	public String getSeasonNumberAndTitleSeparator()
	{
		return seasonNumberAndTitleSeparator;
	}

	public void setSeasonNumberAndTitleSeparator(String seasonNumberAndTitleSeparator)
	{
		this.seasonNumberAndTitleSeparator = seasonNumberAndTitleSeparator;
	}

	public String getSeasonNumberAndEpisodeNumberSeparator()
	{
		return seasonNumberAndEpisodeNumberSeparator;
	}

	public void setSeasonNumberAndEpisodeNumberSeparator(String seasonNumberAndEpisodeNumberSeparator)
	{
		this.seasonNumberAndEpisodeNumberSeparator = seasonNumberAndEpisodeNumberSeparator;
	}

	public String getSeasonAndEpisodeSeparator()
	{
		return seasonAndEpisodeSeparator;
	}

	public void setSeasonAndEpisodeSeparator(String seasonAndEpisodeSeparator)
	{
		this.seasonAndEpisodeSeparator = seasonAndEpisodeSeparator;
	}

	public String getEpisodeNumberAndTitleSeparator()
	{
		return episodeNumberAndTitleSeparator;
	}

	public void setEpisodeNumberAndTitleSeparator(String episodeNumberAndTitleSeparator)
	{
		this.episodeNumberAndTitleSeparator = episodeNumberAndTitleSeparator;
	}

	// placeholders
	public String getUndefinedSeasonPlaceholder()
	{
		return undefinedSeasonPlaceholder;
	}

	public void setUndefinedSeasonPlaceholder(String undefinedSeasonPlaceholder)
	{
		this.undefinedSeasonPlaceholder = undefinedSeasonPlaceholder;
	}

	@Override
	public String doName(Episode epi, NamingService namingService, Map<String, Object> params)
	{
		// settings
		boolean includeSeries = Namings.readParameter(params, PARAM_INCLUDE_SERIES_KEY, Boolean.class, PARAM_INCLUDE_SERIES_DEFAULT);
		boolean includeSeason = Namings.readParameter(params, PARAM_INCLUDE_SEASON_KEY, Boolean.class, PARAM_INCLUDE_SEASON_DEFAULT);

		StringBuilder sb = new StringBuilder();
		if (includeSeries)
		{
			if (epi.getSeries().getName() != null)
			{
				sb.append(formatSeriesName(epi.getSeries().getName()));
			}
			else
			{
				sb.append(undefinedSeriesPlaceholder);
			}
			sb.append(seriesAndAnythingSeparator);
		}

		// if in season, append numberInSeason
		if (epi.isPartOfSeason())
		{
			if (epi.getSeason().isNumbered())
			{
				if (epi.isNumberedInSeason())
				{
					if (includeSeason)
					{
						sb.append(formatSeasonNumber(epi.getSeason().getNumber()));
						if (alwaysIncludeSeasonTitle && epi.getSeason().isTitled())
						{
							sb.append(seasonNumberAndTitleSeparator);
							sb.append(formatSeasonTitle(epi.getSeason().getTitle()));
							sb.append(seasonAndEpisodeSeparator);
						}
						else
						{
							sb.append(seasonNumberAndEpisodeNumberSeparator);
						}
						sb.append(formatEpisodeNumber(epi.getNumberInSeason()));
					}
					else
					{
						sb.append(formatEpisodeNumber(epi.getNumberInSeason()));
					}
					if (alwaysIncludeEpisodeTitle)
					{
						sb.append(episodeNumberAndTitleSeparator);
						sb.append(formatEpisodeTitle(epi.getTitle()));
					}
				}
				else
				{
					if (includeSeason)
					{
						sb.append(formatSeasonNumber(epi.getSeason().getNumber()));
						if (alwaysIncludeSeasonTitle && epi.getSeason().isTitled())
						{
							sb.append(seasonNumberAndTitleSeparator);
							sb.append(formatSeasonTitle(epi.getSeason().getTitle()));
						}
						sb.append(seasonAndEpisodeSeparator);
					}
					if (epi.isTitled())
					{
						sb.append(formatEpisodeTitle(epi.getTitle()));
					}
					else
					{
						sb.append(undefinedEpisodePlaceholder);
					}
				}
			}
			else if (epi.getSeason().isTitled())
			{
				if (includeSeason)
				{
					sb.append(formatSeasonTitle(epi.getSeason().getTitle()));
					sb.append(seasonAndEpisodeSeparator);
				}
				if (epi.isNumberedInSeason())
				{
					sb.append(formatEpisodeNumber(epi.getNumberInSeason()));
					if (alwaysIncludeEpisodeTitle)
					{
						sb.append(episodeNumberAndTitleSeparator);
						sb.append(formatEpisodeTitle(epi.getTitle()));
					}
				}
				else if (epi.isTitled())
				{
					sb.append(formatEpisodeTitle(epi.getTitle()));
				}
				else
				{
					sb.append(undefinedEpisodePlaceholder);
				}
			}
			else
			{
				if (includeSeason)
				{
					sb.append(undefinedSeasonPlaceholder);
					sb.append(seasonAndEpisodeSeparator);
				}
				if (epi.isNumberedInSeason())
				{
					sb.append(formatEpisodeNumber(epi.getNumberInSeason()));
					if (alwaysIncludeEpisodeTitle)
					{
						sb.append(episodeNumberAndTitleSeparator);
						sb.append(formatEpisodeTitle(epi.getTitle()));
					}
				}
				else if (epi.isTitled())
				{
					sb.append(formatEpisodeTitle(epi.getTitle()));
				}
				else
				{
					sb.append(undefinedEpisodePlaceholder);
				}
			}
		}
		else
		{
			if (epi.isNumberedInSeries())
			{
				sb.append(formatEpisodeNumber(epi.getNumberInSeries()));
				if (alwaysIncludeEpisodeTitle)
				{
					sb.append(episodeNumberAndTitleSeparator);
					sb.append(formatEpisodeTitle(epi.getTitle()));
				}
			}
			else if (epi.isTitled())
			{
				sb.append(formatEpisodeTitle(epi.getTitle()));
			}
			else
			{
				sb.append(undefinedEpisodePlaceholder);
			}
		}
		return sb.toString();
	}

	public String formatSeasonNumber(int seasonNumber)
	{
		return String.format(seasonNumberFormat, seasonNumber);
	}

	public String formatSeasonTitle(String seasonTitle)
	{
		return String.format(seasonTitleFormat, Replacer.replace(seasonTitle, seasonTitleReplacer));
	}

	// both numberInSeries and numberInSeason
	public String formatEpisodeNumber(int episodeNumber)
	{
		return String.format(episodeNumberFormat, episodeNumber);
	}
}
