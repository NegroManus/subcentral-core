package de.subcentral.core.naming;

import de.subcentral.core.media.Episode;
import de.subcentral.core.util.Replacer;
import de.subcentral.core.util.StringUtil;

/**
 * 
 * Possible naming combinations:
 * <p>
 * <b>Series, Season, Episode</b>
 * <ul>
 * <li>series seasonnum seasontitle epinum epititle</li>
 * <li>series seasonnum seasontitle epinum</li>
 * <li>series seasonnum seasontitle epititle</li>
 * <li>series seasonnum epinum epititle</li>
 * <li>series seasonnum epinum</li>
 * <li>series seasonnum epititle</li>
 * <li>series seasontitle epinum epititle</li>
 * <li>series seasontitle epinum</li>
 * <li>series seasontitle epititle</li>
 * <li>series Sxx epinum epititle</li>
 * <li>series Sxx epinum</li>
 * <li>series Sxx epititle</li>
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
 * <li>seasonnum epinum epititle</li>
 * <li>seasonnum epinum</li>
 * <li>seasonnum epititle</li>
 * <li>seasontitle epinum epititle</li>
 * <li>seasontitle epinum</li>
 * <li>seasontitle epititle</li>
 * <li>Sxx epinum epititle</li>
 * <li>Sxx epinum</li>
 * <li>Sxx epititle</li>
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
 * @author mhertram
 *
 */
public class SeriesEpisodeNamer extends AbstractEpisodeNamer
{
	private String		seasonEpisodeNumberFormat	= " S%02dE%02d";
	private String		seasonNumberFormat			= " S%02d";
	private Replacer	seasonTitleReplacer			= null;
	private String		seasonTitleFormat			= " %s";
	private String		episodeNumberFormat			= " E%02d";

	public String getSeasonEpisodeNumberFormat()
	{
		return seasonEpisodeNumberFormat;
	}

	public void setSeasonEpisodeNumberFormat(String seasonEpisodeNumberFormat)
	{
		this.seasonEpisodeNumberFormat = seasonEpisodeNumberFormat;
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
			sb.append(String.format(seriesNameFormat, StringUtil.replace(epi.getSeries().getNameOrCompute(), seriesNameReplacer)));
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
						sb.append(String.format(seasonEpisodeNumberFormat, epi.getSeason().getNumber(), epi.getNumberInSeason()));
					}
					else
					{
						sb.append(String.format(episodeNumberFormat, epi.getNumberInSeason()));
					}
					if (alwaysIncludeEpisodeTitle)
					{
						sb.append(String.format(episodeTitleFormat, StringUtil.replace(epi.getTitle(), episodeTitleReplacer)));
					}
				}
				else
				{
					if (includeSeason)
					{
						sb.append(String.format(seasonNumberFormat, epi.getSeason().getNumber()));
					}
					if (epi.isTitled())
					{
						sb.append(String.format(episodeTitleFormat, StringUtil.replace(epi.getTitle(), episodeTitleReplacer)));
					}
					else
					{
						sb.append(String.format(episodeTitleFormat, "xx"));
					}
				}
			}
			else if (epi.getSeason().isTitled())
			{
				if (includeSeason)
				{
					sb.append(String.format(seasonTitleFormat, StringUtil.replace(epi.getSeason().getTitle(), seasonTitleReplacer)));
				}
				if (epi.isNumberedInSeason())
				{
					sb.append(String.format(episodeNumberFormat, epi.getNumberInSeason()));
					if (alwaysIncludeEpisodeTitle)
					{
						sb.append(String.format(episodeTitleFormat, StringUtil.replace(epi.getTitle(), episodeTitleReplacer)));
					}
				}
				else if (epi.isTitled())
				{
					sb.append(String.format(episodeTitleFormat, StringUtil.replace(epi.getTitle(), episodeTitleReplacer)));
				}
				else
				{
					sb.append(String.format(episodeTitleFormat, "xx"));
				}
			}
			else
			{
				sb.append(String.format(seasonTitleFormat, "xx"));
			}
		}
		else
		{
			if (epi.isNumberedInSeries())
			{
				sb.append(String.format(episodeNumberFormat, epi.getNumberInSeries()));
				if (alwaysIncludeEpisodeTitle)
				{
					sb.append(String.format(episodeTitleFormat, episodeTitleReplacer.process(epi.getTitle())));
				}
			}
			else if (epi.isTitled())
			{
				sb.append(String.format(episodeTitleFormat, episodeTitleReplacer.process(epi.getTitle())));
			}
			else
			{
				sb.append(String.format(episodeTitleFormat, "xx"));
			}
		}
		return sb.toString();
	}
}
