package de.subcentral.mig.process.old;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.correct.Correction;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.name.NamingDefaults;
import de.subcentral.core.name.NamingUtil;
import de.subcentral.mig.process.Subber;

public class Repository
{
	private static final Logger log = LogManager.getLogger(Repository.class);

	private final SortedSet<Series>				series		= new TreeSet<>();
	private final SortedSet<Season>				seasons		= new TreeSet<>();
	private final SortedSet<Episode>			episodes	= new TreeSet<>();
	private final SortedSet<Subber>				subbers		= new TreeSet<>();
	private final SortedSet<Release>			releases	= new TreeSet<>();
	private final SortedSet<SubtitleRelease>	subtitles	= new TreeSet<>();

	public SortedSet<Series> getSeries()
	{
		return series;
	}

	public SortedSet<Season> getSeasons()
	{
		return seasons;
	}

	public SortedSet<Episode> getEpisodes()
	{
		return episodes;
	}

	public SortedSet<Subber> getSubbers()
	{
		return subbers;
	}

	public SortedSet<Release> getReleases()
	{
		return releases;
	}

	public SortedSet<SubtitleRelease> getSubtitles()
	{
		return subtitles;
	}

	public Optional<Series> findSeries(Series candidate)
	{
		List<Correction> changes = MigrationSettings.INSTANCE.getStandardizingService().correct(candidate);
		changes.stream().forEach((c) -> log.debug("Changed Series {}", c));
		return series.stream().filter(NamingUtil.filterByName(candidate, NamingDefaults.getDefaultNormalizingNamingService(), MigrationSettings.INSTANCE.getNamingParams())).findAny();
	}

	public static void main(String[] args)
	{
		Repository repo = new Repository();
		repo.getSeries().add(new Series("Psych"));
		repo.getSeries().add(new Series("How I Met Your Mother"));
		repo.getSeries().add(new Series("Game of Thrones"));
		repo.getSeries().add(new Series("Veep"));

		System.out.println(repo.findSeries(new Series("how.i.met.your.mother")));
	}
}
