package de.subcentral.core.naming;

import de.subcentral.core.media.Episode;
import de.subcentral.core.util.Replacer;
import de.subcentral.core.util.StringUtil;

public abstract class AbstractEpisodeNamer implements EpisodeNamer
{
	protected boolean	alwaysIncludeEpisodeTitle	= false;

	protected Replacer	seriesNameReplacer			= null;
	protected String	seriesNameFormat			= "%s";
	protected Replacer	episodeTitleReplacer		= null;
	protected String	episodeTitleFormat			= "%s";

	protected String	separatorSeriesAndAnything	= " ";

	public Replacer getSeriesNameReplacer()
	{
		return seriesNameReplacer;
	}

	public void setSeriesNameReplacer(Replacer seriesNameReplacer)
	{
		this.seriesNameReplacer = seriesNameReplacer;
	}

	public String getSeriesNameFormat()
	{
		return seriesNameFormat;
	}

	public void setSeriesNameFormat(String seriesNameFormat)
	{
		this.seriesNameFormat = seriesNameFormat;
	}

	public String getSeparatorSeriesAndAnything()
	{
		return separatorSeriesAndAnything;
	}

	public void setSeparatorSeriesAndAnything(String separatorSeriesAndAnything)
	{
		this.separatorSeriesAndAnything = separatorSeriesAndAnything;
	}

	public boolean getAlwaysIncludeEpisodeTitle()
	{
		return alwaysIncludeEpisodeTitle;
	}

	public void setAlwaysIncludeEpisodeTitle(boolean alwaysIncludeEpisodeTitle)
	{
		this.alwaysIncludeEpisodeTitle = alwaysIncludeEpisodeTitle;
	}

	public Replacer getEpisodeTitleReplacer()
	{
		return episodeTitleReplacer;
	}

	public void setEpisodeTitleReplacer(Replacer episodeTitleReplacer)
	{
		this.episodeTitleReplacer = episodeTitleReplacer;
	}

	public String getEpisodeTitleFormat()
	{
		return episodeTitleFormat;
	}

	public void setEpisodeTitleFormat(String episodeTitleFormat)
	{
		this.episodeTitleFormat = episodeTitleFormat;
	}

	@Override
	public Class<Episode> getType()
	{
		return Episode.class;
	}

	@Override
	public String name(Episode candidate, NamingService namingService) throws NamingException
	{
		return name(candidate, true, true, namingService);
	}

	public String formatSeriesName(String seriesName)
	{
		return String.format(seriesNameFormat, StringUtil.replace(seriesName, seriesNameReplacer));
	}

	public String formatEpisodeTitle(String episodeTitle)
	{
		return String.format(episodeTitleFormat, StringUtil.replace(episodeTitle, episodeTitleReplacer));
	}
}
