package de.subcentral.core.naming;

import de.subcentral.core.media.Episode;
import de.subcentral.core.util.Replacer;

public class SeriesEpisodeNamer implements Namer<Episode>
{
	private Replacer	seriesNameReplacer			= NamingStandards.STANDARD_REPLACER;
	private String		seriesFormat				= "%s";
	private String		seasonNumberFormat			= ".S%02d";
	private String		seasonEpisodeNumberFormat	= ".S%02dE%02d";
	private Replacer	seasonTitleReplacer			= NamingStandards.STANDARD_REPLACER;
	private String		seasonTitleFormat			= ".%s";
	private String		episodeNumberFormat			= ".E%02d";
	private boolean		alwaysIncludeEpisodeTitle	= false;
	private Replacer	episodeTitleReplacer		= NamingStandards.STANDARD_REPLACER;
	private String		episodeTitleFormat			= ".%s";

	@Override
	public Class<Episode> getType()
	{
		return Episode.class;
	}

	@Override
	public String name(Episode epi, NamingService namingService)
	{
		if (epi == null)
		{
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(String.format(seriesFormat, seriesNameReplacer.process(epi.getSeries().getName())));
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
						sb.append(String.format(episodeTitleFormat, episodeTitleReplacer.process(epi.getTitle())));
					}
				}
				else if (epi.isTitled())
				{
					sb.append(String.format(seasonNumberFormat, epi.getSeason().getNumber()));
					sb.append(String.format(episodeTitleFormat, episodeTitleReplacer.process(epi.getTitle())));
				}
				else
				{
					sb.append(String.format(seasonNumberFormat, epi.getSeason().getNumber()));
					sb.append(String.format(episodeTitleFormat, "xx"));
				}
			}
			else if (epi.getSeason().isTitled())
			{
				sb.append(String.format(seasonTitleFormat, seasonTitleReplacer.process(epi.getSeason().getTitle())));
				if (epi.isNumberedInSeason())
				{
					sb.append(String.format(episodeNumberFormat, epi.getNumberInSeason()));
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
