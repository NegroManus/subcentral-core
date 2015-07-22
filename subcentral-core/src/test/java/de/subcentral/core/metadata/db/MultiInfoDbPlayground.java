package de.subcentral.core.metadata.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.MultiEpisodeHelper;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.ReleaseUtil;
import de.subcentral.core.naming.ConditionalNamingService;
import de.subcentral.core.naming.ConditionalNamingService.ConditionalNamingEntry;
import de.subcentral.core.naming.MultiEpisodeNamer;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.PropSequenceNameBuilder.Config;
import de.subcentral.core.util.Separation;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.orlydbcom.OrlyDbComReleaseDb;
import de.subcentral.support.predbme.PreDbMeReleaseDb;
import de.subcentral.support.xrelto.XRelToReleaseDb;

public class MultiInfoDbPlayground
{

    /**
     * <pre>
     * -Dhttp.proxyHost=10.206.247.65
     * -Dhttp.proxyHost=10.206.247.65
     * </pre>
     * 
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException
    {
	PreDbMeReleaseDb preDbMe = new PreDbMeReleaseDb();
	XRelToReleaseDb xrelTo = new XRelToReleaseDb();
	OrlyDbComReleaseDb orlyDb = new OrlyDbComReleaseDb();
	List<MetadataDb<Release>> infoDbs = new ArrayList<>(3);
	infoDbs.add(preDbMe);
	infoDbs.add(xrelTo);
	infoDbs.add(orlyDb);

	ConditionalNamingService alternateNs = new ConditionalNamingService("alternate");
	Config cfg = new Config();
	cfg.setSeparations(ImmutableSet.of(Separation.between(Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON, "")));
	MultiEpisodeNamer alternameMeNamer = new MultiEpisodeNamer(cfg);
	alternateNs.getConditionalNamingEntries().add(ConditionalNamingEntry.of(MultiEpisodeHelper::isMultiEpisode, alternameMeNamer));
	NamingService alternateMetadataDbNs = NamingDefaults.createNormalizingNamingService(alternateNs);
	ImmutableList<NamingService> namingServices = ImmutableList.of(NamingDefaults.getDefaultNormalizingNamingService(), alternateMetadataDbNs);

	Episode epi1 = Episode.createSeasonedEpisode("How I Met Your Mother", 9, 23);
	Episode epi2 = Episode.createSeasonedEpisode("How I Met Your Mother", 9, 24);
	List<Episode> query = ImmutableList.of(epi1, epi2);
	// RegularMedia query = new RegularMedia("Halo.Nightfall");

	ExecutorService executor = Executors.newFixedThreadPool(3);

	System.out.println("Querying");
	long start = System.nanoTime();
	ListMultimap<MetadataDb<Release>, Release> results = MetadataDbUtil.queryAll(infoDbs, query, namingServices, executor);
	TimeUtil.printDurationMillis("queryAll", start);
	for (Map.Entry<MetadataDb<Release>, Collection<Release>> entry : results.asMap().entrySet())
	{
	    System.out.println("Results of " + entry.getKey().getName() + " " + entry.getKey().getDomain());
	    entry.getValue().stream().forEach((r) -> System.out.println(r));
	    System.out.println();
	}
	executor.shutdown();

	List<Release> reducedRlss = ReleaseUtil.distinctByName(results.values());
	reducedRlss.stream().forEach(e -> System.out.println(e));
    }
}
