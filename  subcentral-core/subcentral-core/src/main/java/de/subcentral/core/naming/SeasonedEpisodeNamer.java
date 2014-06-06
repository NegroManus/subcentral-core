package de.subcentral.core.naming;

import de.subcentral.core.media.Episode;
import de.subcentral.core.util.Replacer;
import de.subcentral.core.util.StringUtil;

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
	private boolean		alwaysIncludeSeasonTitle				= false;

	private String		seasonNumberFormat						= "S%02d";
	private Replacer	seasonTitleReplacer						= null;
	private String		seasonTitleFormat						= "%s";
	private String		episodeNumberFormat						= "E%02d";

	private String		separatorSeriesAndAnything				= " ";
	private String		separatorSeasonNumberAndTitle			= " ";
	private String		separatorSeasonNumberAndEpisodeNumber	= "";
	private String		separatorSeasonAndEpisode				= " ";
	private String		separatorEpisodeNumberAndTitle			= " ";

	private String		undefinedSeries							= "UNNAMED_SERIES";
	private String		undefinedSeason							= "Sxx";
	private String		undefinedEpisode						= "Exx";

	public boolean getAlwaysIncludeSeasonTitle()
	{
		return alwaysIncludeSeasonTitle;
	}

	public void setAlwaysIncludeSeasonTitle(boolean alwaysIncludeSeasonTitle)
	{
		this.alwaysIncludeSeasonTitle = alwaysIncludeSeasonTitle;
	}

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

	@Override
	public String name(Episode epi, boolean includeSeries, boolean includeSeason, NamingService namingService)
	{
		if (epi == null)
		{
			return null;
		}

		StringBuilder sb = new StringBuilder();
		if (includeSeries)
		{
			if (epi.getSeries().isNameSet())
			{
				sb.append(formatSeriesName(epi.getSeries().getName()));
			}
			else
			{
				sb.append(undefinedSeries);
			}
			sb.append(separatorSeriesAndAnything);
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
							sb.append(separatorSeasonNumberAndTitle);
							sb.append(formatSeasonTitle(epi.getSeason().getTitle()));
							sb.append(separatorSeasonAndEpisode);
						}
						else
						{
							sb.append(separatorSeasonNumberAndEpisodeNumber);
						}
						sb.append(formatEpisodeNumber(epi.getNumberInSeason()));
					}
					else
					{
						sb.append(formatEpisodeNumber(epi.getNumberInSeason()));
					}
					if (alwaysIncludeEpisodeTitle)
					{
						sb.append(separatorEpisodeNumberAndTitle);
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
							sb.append(separatorSeasonNumberAndTitle);
							sb.append(formatSeasonTitle(epi.getSeason().getTitle()));
						}
						sb.append(separatorSeasonAndEpisode);
					}
					if (epi.isTitled())
					{
						sb.append(formatEpisodeTitle(epi.getTitle()));
					}
					else
					{
						sb.append(undefinedEpisode);
					}
				}
			}
			else if (epi.getSeason().isTitled())
			{
				if (includeSeason)
				{
					sb.append(formatSeasonTitle(epi.getSeason().getTitle()));
					sb.append(separatorSeasonAndEpisode);
				}
				if (epi.isNumberedInSeason())
				{
					sb.append(formatEpisodeNumber(epi.getNumberInSeason()));
					if (alwaysIncludeEpisodeTitle)
					{
						sb.append(separatorEpisodeNumberAndTitle);
						sb.append(formatEpisodeTitle(epi.getTitle()));
					}
				}
				else if (epi.isTitled())
				{
					sb.append(formatEpisodeTitle(epi.getTitle()));
				}
				else
				{
					sb.append(undefinedEpisode);
				}
			}
			else
			{
				if (includeSeason)
				{
					sb.append(undefinedSeason);
					sb.append(separatorSeasonAndEpisode);
				}
				if (epi.isNumberedInSeason())
				{
					sb.append(formatEpisodeNumber(epi.getNumberInSeason()));
					if (alwaysIncludeEpisodeTitle)
					{
						sb.append(separatorEpisodeNumberAndTitle);
						sb.append(formatEpisodeTitle(epi.getTitle()));
					}
				}
				else if (epi.isTitled())
				{
					sb.append(formatEpisodeTitle(epi.getTitle()));
				}
				else
				{
					sb.append(undefinedEpisode);
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
					sb.append(separatorEpisodeNumberAndTitle);
					sb.append(formatEpisodeTitle(epi.getTitle()));
				}
			}
			else if (epi.isTitled())
			{
				sb.append(formatEpisodeTitle(epi.getTitle()));
			}
			else
			{
				sb.append(undefinedEpisode);
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
		return String.format(seasonTitleFormat, StringUtil.replace(seasonTitle, seasonTitleReplacer));
	}

	// both numberInSeries and numberInSeason
	public String formatEpisodeNumber(int episodeNumber)
	{
		return String.format(episodeNumberFormat, episodeNumber);
	}
}
