package de.subcentral.core.naming;

import de.subcentral.core.media.MultiEpisode;
import de.subcentral.core.media.Season;
import de.subcentral.core.media.Series;
import de.subcentral.core.util.Replacer;

public class MultiEpisodeNamer extends AbstractNamer<MultiEpisode>
{
	private EpisodeNamer	episodeNamer				= NamingStandards.SEASONED_EPISODE_NAMER;

	private Replacer		seriesNameReplacer			= null;
	private String			seriesNameFormat			= "%s";
	private String			seasonEpisodeNumberFormat	= " S%02dE%02d";
	private String			seasonNumberFormat			= " S%02d";
	private Replacer		seasonTitleReplacer			= null;
	private String			seasonTitleFormat			= " %s";
	private String			episodeNumberFormat			= " E%02d";
	private boolean			alwaysIncludeEpisodeTitle	= false;
	private Replacer		episodeTitleReplacer		= null;
	private String			episodeTitleFormat			= " %s";

	private String			episodeNumberRangeFormat	= "%s-%s";
	private String			episodeNumberAdditionFormat	= "%s";
	private String			episodeAdditionFormat		= " %s";

	@Override
	public Class<MultiEpisode> getType()
	{
		return MultiEpisode.class;
	}

	@Override
	protected String doName(MultiEpisode me, NamingService namingService) throws Exception
	{
		if (me.isEmpty())
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Series commonSeries = me.getCommonSeries();
		if (commonSeries != null)
		{
			Season commonSeason = me.getCommonSeason();
			if (commonSeason != null)
			{
				sb.append(namingService.name(me.get(0)));
				for (int i = 1; i < me.size(); i++)
				{
					sb.append(String.format(episodeAdditionFormat, episodeNamer.name(me.get(i), false, false)));
				}
			}
			else
			{
				sb.append(namingService.name(me.get(0)));
				for (int i = 1; i < me.size(); i++)
				{
					sb.append(String.format(episodeAdditionFormat, episodeNamer.name(me.get(i), false, true)));
				}
			}
		}
		else
		{
			sb.append(namingService.name(me.get(0)));
			for (int i = 1; i < me.size(); i++)
			{
				sb.append(String.format(episodeAdditionFormat, namingService.name(me.get(i))));
			}
		}
		return sb.toString();
	}
}
