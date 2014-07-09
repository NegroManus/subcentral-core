package de.subcentral.core.naming;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.util.Replacer;

public abstract class AbstractEpisodeNamer extends AbstractNamer<Episode>
{
	protected boolean	alwaysIncludeEpisodeTitle	= false;

	protected Replacer	seriesNameReplacer			= null;
	protected String	seriesNameFormat			= "%s";
	protected Replacer	episodeTitleReplacer		= null;
	protected String	episodeTitleFormat			= "%s";

	protected String	seriesAndAnythingSeparator	= " ";

	protected String	undefinedSeriesPlaceholder	= "UNNAMED_SERIES";
	protected String	undefinedEpisodePlaceholder	= "Exx";

	// booleans
	public boolean getAlwaysIncludeEpisodeTitle()
	{
		return alwaysIncludeEpisodeTitle;
	}

	public void setAlwaysIncludeEpisodeTitle(boolean alwaysIncludeEpisodeTitle)
	{
		this.alwaysIncludeEpisodeTitle = alwaysIncludeEpisodeTitle;
	}

	// replacer and formats
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

	// separators
	public String getSeriesAndAnythingSeparator()
	{
		return seriesAndAnythingSeparator;
	}

	public void setSeriesAndAnythingSeparator(String seriesAndAnythingSeparator)
	{
		this.seriesAndAnythingSeparator = seriesAndAnythingSeparator;
	}

	@Override
	public Class<Episode> getType()
	{
		return Episode.class;
	}

	// format methods
	public String formatSeriesName(String seriesName)
	{
		return String.format(seriesNameFormat, Replacer.replace(seriesName, seriesNameReplacer));
	}

	public String formatEpisodeTitle(String episodeTitle)
	{
		return String.format(episodeTitleFormat, Replacer.replace(episodeTitle, episodeTitleReplacer));
	}
}
