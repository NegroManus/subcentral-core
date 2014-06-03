package de.subcentral.core.impl.scene;

import de.subcentral.core.media.Episode;
import de.subcentral.core.media.Series;
import de.subcentral.core.naming.Namer;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.util.Replacer;
import de.subcentral.core.util.StringUtil;

public class SceneEpisodeNamer implements Namer<Episode>
{
	private String		assumedSeriesType			= null;
	private boolean		alwaysIncludeEpisodeTitle	= false;
	private Replacer	replacer					= null;

	public String getAssumedSeriesType()
	{
		return assumedSeriesType;
	}

	public void setAssumedSeriesType(String assumedSeriesType)
	{
		this.assumedSeriesType = assumedSeriesType;
	}

	public boolean getAlwaysIncludeEpisodeTitle()
	{
		return alwaysIncludeEpisodeTitle;
	}

	public void setAlwaysIncludeEpisodeTitle(boolean alwaysIncludeEpisodeTitle)
	{
		this.alwaysIncludeEpisodeTitle = alwaysIncludeEpisodeTitle;
	}

	public Replacer getReplacer()
	{
		return replacer;
	}

	public void setReplacer(Replacer replacer)
	{
		this.replacer = replacer;
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
		String type = assumedSeriesType != null ? assumedSeriesType : epi.getSeries().getType();
		if (type == Series.TYPE_DATED_SHOW)
		{
			return nameDatedShowEpisode(epi);
		}
		if (type == Series.TYPE_MINI_SERIES)
		{
			return nameMiniseriesEpisode(epi);
		}
		return nameSeriesEpisode(epi);
	}

	public String nameSeriesEpisode(Episode epi)
	{
		if (epi == null)
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.replace(epi.getSeries().getName(), replacer));
		// if in season, append numberInSeason
		if (epi.isPartOfSeason())
		{
			if (epi.getSeason().isNumbered())
			{
				sb.append('.');
				sb.append('S');
				sb.append(String.format("%02d", epi.getSeason().getNumber()));
			}
			else if (epi.getSeason().isTitled())
			{
				sb.append('.');
				sb.append(epi.getSeason().getTitle());
			}
			if (epi.isNumberedInSeason())
			{
				sb.append('E');
				sb.append(String.format("%02d", epi.getNumberInSeason()));
			}
			if (alwaysIncludeEpisodeTitle && epi.isTitled())
			{
				sb.append('.');
				sb.append(StringUtil.replace(epi.getTitle(), replacer));
			}
		}
		else if (epi.isNumberedInSeries())
		{
			sb.append('.');
			sb.append('E');
			sb.append(String.format("%02d", epi.getNumberInSeries()));
			if (alwaysIncludeEpisodeTitle && epi.isTitled())
			{
				sb.append('.');
				sb.append(StringUtil.replace(epi.getTitle(), replacer));
			}
		}
		else if (epi.isTitled())
		{
			sb.append('.');
			sb.append(StringUtil.replace(epi.getTitle(), replacer));
		}
		return sb.toString();
	}

	public String nameDatedShowEpisode(Episode epi)
	{
		if (epi == null)
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.replace(epi.getSeries().getName(), replacer));
		if (epi.getDate() != null)
		{
			sb.append('.');
			sb.append(epi.getDate());
		}
		return sb.toString();
	}

	public String nameMiniseriesEpisode(Episode epi)
	{
		if (epi == null)
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.replace(epi.getSeries().getName(), replacer));
		if (epi.isNumberedInSeason())
		{
			sb.append(".Part");
			sb.append(String.format("%02d", epi.getNumberInSeason()));
		}
		else if (epi.isNumberedInSeries())
		{
			sb.append(".Part");
			sb.append(String.format("%02d", epi.getNumberInSeries()));
		}
		return sb.toString();
	}

}
