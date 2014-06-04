package de.subcentral.core.naming;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import de.subcentral.core.media.Episode;
import de.subcentral.core.util.Replacer;
import de.subcentral.core.util.StringUtil;

public class MiniSeriesEpisodeNamer implements Namer<Episode>
{
	private Replacer	seriesNameReplacer			= NamingStandards.STANDARD_REPLACER;
	private String		episodeNumberPrefix			= ".Part";
	private String		episodeNumberFormat			= "00";
	private boolean		alwaysIncludeEpisodeTitle	= false;
	private String		episodeTitlePrefix			= ".";
	private Replacer	episodeTitleReplacer		= NamingStandards.STANDARD_REPLACER;

	public Replacer getSeriesNameReplacer()
	{
		return seriesNameReplacer;
	}

	public void setSeriesNameReplacer(Replacer seriesNameReplacer)
	{
		this.seriesNameReplacer = seriesNameReplacer;
	}

	public String getEpisodeNumberPrefix()
	{
		return episodeNumberPrefix;
	}

	public void setEpisodeNumberPrefix(String episodeNumberPrefix)
	{
		this.episodeNumberPrefix = episodeNumberPrefix;
	}

	public String getEpisodeNumberFormat()
	{
		return episodeNumberFormat;
	}

	public void setEpisodeNumberFormat(String episodeNumberFormat)
	{
		this.episodeNumberFormat = episodeNumberFormat;
	}

	public boolean getAlwaysIncludeEpisodeTitle()
	{
		return alwaysIncludeEpisodeTitle;
	}

	public void setAlwaysIncludeEpisodeTitle(boolean alwaysIncludeEpisodeTitle)
	{
		this.alwaysIncludeEpisodeTitle = alwaysIncludeEpisodeTitle;
	}

	public String getEpisodeTitlePrefix()
	{
		return episodeTitlePrefix;
	}

	public void setEpisodeTitlePrefix(String episodeTitlePrefix)
	{
		this.episodeTitlePrefix = episodeTitlePrefix;
	}

	public Replacer getEpisodeTitleReplacer()
	{
		return episodeTitleReplacer;
	}

	public void setEpisodeTitleReplacer(Replacer episodeTitleReplacer)
	{
		this.episodeTitleReplacer = episodeTitleReplacer;
	}

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

		NumberFormat episodeNumFormat = new DecimalFormat(episodeNumberFormat);

		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.replace(epi.getSeries().getName(), seriesNameReplacer));
		sb.append(episodeNumberPrefix);
		sb.append(episodeNumFormat.format(epi.getNumberInSeries()));
		if (alwaysIncludeEpisodeTitle && epi.isTitled())
		{
			sb.append(episodeTitlePrefix);
			sb.append(StringUtil.replace(epi.getTitle(), episodeTitleReplacer));
		}
		return sb.toString();
	}

}
