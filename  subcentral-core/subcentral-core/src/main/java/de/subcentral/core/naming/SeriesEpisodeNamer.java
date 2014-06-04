package de.subcentral.core.naming;

import de.subcentral.core.media.Episode;
import de.subcentral.core.util.Replacer;
import de.subcentral.core.util.StringUtil;

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
	public String name(Episode epi, NamingService namingService)
	{
		if (epi == null)
		{
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(String.format(seriesNameFormat, StringUtil.replace(epi.getSeries().getNameOrCompute(), seriesNameReplacer)));
		// if in season, append numberInSeason
		if (epi.isPartOfSeason())
		{
			if (epi.getSeason().isNumbered())
			{
				if (epi.isNumberedInSeason())
				{
					sb.append(String.format(seasonEpisodeNumberFormat, epi.getSeason().getNumber(), epi.getNumberInSeason()));
					if (alwaysIncludeEpisodeTitle)
					{
						sb.append(String.format(episodeTitleFormat, StringUtil.replace(epi.getTitle(), episodeTitleReplacer)));
					}
				}
				else if (epi.isTitled())
				{
					sb.append(String.format(seasonNumberFormat, epi.getSeason().getNumber()));
					sb.append(String.format(episodeTitleFormat, StringUtil.replace(epi.getTitle(), episodeTitleReplacer)));
				}
				else
				{
					sb.append(String.format(seasonNumberFormat, epi.getSeason().getNumber()));
					sb.append(String.format(episodeTitleFormat, "xx"));
				}
			}
			else if (epi.getSeason().isTitled())
			{
				sb.append(String.format(seasonTitleFormat, StringUtil.replace(epi.getTitle(), episodeTitleReplacer)));
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
