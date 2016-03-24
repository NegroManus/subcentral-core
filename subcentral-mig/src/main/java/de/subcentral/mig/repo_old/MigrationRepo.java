package de.subcentral.mig.repo_old;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MigrationRepo
{
	// Series->ID
	private Map<SeriesKey, Integer>		series			= new HashMap<>();
	// Season->ID
	private Map<SeasonKey, Integer>		seasons			= new HashMap<>();
	// Season->ID
	private Map<EpisodeKey, Integer>	episodes		= new HashMap<>();
	// Network->ID
	private Map<NetworkKey, Integer>	networks		= new HashMap<>();

	// <AttachmentID>
	private Set<Integer>				attachments		= new HashSet<>();
	// <Hash->SubtitleFileID>
	private Map<Long, Integer>			subtitleFiles	= new HashMap<>();

	public Map<SeriesKey, Integer> getSeries()
	{
		return series;
	}

	public Map<SeasonKey, Integer> getSeasons()
	{
		return seasons;
	}

	public Map<EpisodeKey, Integer> getEpisodes()
	{
		return episodes;
	}

	public Map<NetworkKey, Integer> getNetworks()
	{
		return networks;
	}
}
