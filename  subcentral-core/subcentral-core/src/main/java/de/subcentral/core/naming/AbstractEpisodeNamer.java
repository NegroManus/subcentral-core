package de.subcentral.core.naming;

import de.subcentral.core.media.Episode;
import de.subcentral.core.util.Replacer;

public abstract class AbstractEpisodeNamer implements EpisodeNamer
{
	protected Replacer	seriesNameReplacer			= null;
	protected String	seriesNameFormat			= "%s";

	protected boolean	alwaysIncludeEpisodeTitle	= false;
	protected Replacer	episodeTitleReplacer		= null;
	protected String	episodeTitleFormat			= " %s";

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

}
