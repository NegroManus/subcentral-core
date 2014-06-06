package de.subcentral.core.naming;

import de.subcentral.core.media.Episode;

public class MiniSeriesEpisodeNamer extends AbstractEpisodeNamer
{
	private String	episodeNumberFormat				= "Part%02d";

	private String	separatorEpisodeNumberAndTitle	= " ";

	public String getEpisodeNumberFormat()
	{
		return episodeNumberFormat;
	}

	public void setEpisodeNumberFormat(String episodeNumberFormat)
	{
		this.episodeNumberFormat = episodeNumberFormat;
	}

	public String getSeparatorEpisodeNumberAndTitle()
	{
		return separatorEpisodeNumberAndTitle;
	}

	public void setSeparatorEpisodeNumberAndTitle(String separatorEpisodeNumberAndTitle)
	{
		this.separatorEpisodeNumberAndTitle = separatorEpisodeNumberAndTitle;
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
			sb.append(formatSeriesName(epi.getSeries().getName()));
			sb.append(separatorSeriesAndAnything);
		}
		if (epi.isNumberedInSeries())
		{
			sb.append(formatEpisodeNumber(epi.getNumberInSeries()));
			if (alwaysIncludeEpisodeTitle && epi.isTitled())
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
			sb.append(formatEpisodeNumber(0));
		}
		return sb.toString();
	}

	public String formatEpisodeNumber(int episodeNumber)
	{
		return String.format(episodeNumberFormat, episodeNumber);
	}
}
