package de.subcentral.core.naming;

import de.subcentral.core.media.Episode;
import de.subcentral.core.util.StringUtil;

public class MiniSeriesEpisodeNamer extends AbstractEpisodeNamer
{
	private String	episodeNumberFormat	= " Part%02d";

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
		sb.append(String.format(seriesNameFormat, StringUtil.replace(epi.getSeries().getNameOrCompute(), seriesNameReplacer)));
		if (epi.isNumberedInSeries())
		{
			sb.append(String.format(episodeNumberFormat, epi.getNumberInSeries()));
			if (alwaysIncludeEpisodeTitle && epi.isTitled())
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
		return sb.toString();
	}
}
