package de.subcentral.core.parse;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.JavaLookup;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.correct.Correction;
import de.subcentral.core.correct.CorrectionDefaults;
import de.subcentral.core.correct.SeriesNameCorrector;
import de.subcentral.core.correct.TypeBasedCorrectionService;
import de.subcentral.core.metadata.db.MetadataDb;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.release.CompatibilityService.CompatibilityInfo;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.ReleaseUtil;
import de.subcentral.core.metadata.release.SameGroupCompatibility;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.name.NamingDefaults;
import de.subcentral.core.name.NamingService;
import de.subcentral.core.name.NamingUtil;
import de.subcentral.core.name.ReleaseNamer;
import de.subcentral.core.name.SubtitleReleaseNamer;
import de.subcentral.core.parse.TypeBasedParsingService;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.italiansubsnet.ItalianSubsNet;
import de.subcentral.support.orlydbcom.OrlyDbComMetadataDb;
import de.subcentral.support.releasescene.ReleaseScene;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.winrar.WinRar;
import de.subcentral.support.winrar.WinRarPackConfig;
import de.subcentral.support.winrar.WinRarPackConfig.CompressionMethod;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.support.winrar.WinRarPackConfig.OverwriteMode;
import de.subcentral.support.winrar.WinRarPackager;

public class ParsingPlayground
{
	private static final Logger log = LogManager.getLogger(ParsingPlayground.class);

	/**
	 * To specify other watch folder than "<user.home>/Downloads", add argument "watchFolder="D:\Downloads".
	 * 
	 * If behind proxy, add VM args:
	 * 
	 * <pre>
	 * -Dhttp.proxyHost=10.151.249.76 -Dhttp.proxyPort=8080
	 * </pre>
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) throws Exception
	{
		JavaLookup debugJavaLookup = new JavaLookup();
		log.info("Runtime: {}", debugJavaLookup.getRuntime());
		log.info("Virtual machine: {}", debugJavaLookup.getVirtualMachine());
		log.info("Locale: {}", debugJavaLookup.getLocale());
		log.info("Operating system: {}", debugJavaLookup.getOperatingSystem());
		log.info("Hardware: {}", debugJavaLookup.getHardware());

		Path watchFolder = null;
		for (String arg : args)
		{
			if (arg.startsWith("watchFolder="))
			{
				watchFolder = Paths.get(arg.substring(12));
			}
		}
		if (watchFolder == null)
		{
			watchFolder = Paths.get(System.getProperty("user.home"), "Downloads");
		}

		final long totalStart = System.nanoTime();

		final TypeBasedParsingService ps = new TypeBasedParsingService("default");
		// order is relevant. ReleaseScene matchers would also match SubCentralDe matchers
		ps.getParserEntries().addAll(Addic7edCom.getParserEntries());
		ps.getParserEntries().addAll(SubCentralDe.getParserEntries());
		ps.getParserEntries().addAll(ReleaseScene.getParsersEntries());
		ps.getParserEntries().addAll(ItalianSubsNet.getParserEntries());

		final NamingService ns = NamingDefaults.getDefaultNamingService();

		final MetadataDb rlsInfoDb = new OrlyDbComMetadataDb();
		final NamingService mediaNsForFiltering = NamingDefaults.getDefaultNormalizingNamingService();

		final CompatibilityService compService = new CompatibilityService();
		compService.getCompatibilities().add(new SameGroupCompatibility());
		compService.getCompatibilities().add(new CrossGroupCompatibility(new Group("LOL"), new Group("DIMENSION"), true));
		compService.getCompatibilities().add(new CrossGroupCompatibility(new Group("EXCELLENCE"), new Group("REMARKABLE"), true));
		compService.getCompatibilities().add(new CrossGroupCompatibility(new Group("ASAP"), new Group("IMMERSE"), true));

		final WinRarPackConfig packCfg = new WinRarPackConfig();
		packCfg.setSourceDeletionMode(DeletionMode.DELETE);
		packCfg.setTargetOverwriteMode(OverwriteMode.REPLACE);
		packCfg.setCompressionMethod(CompressionMethod.BEST);
		final WinRarPackager packager = WinRar.getInstance().getPackager();

		final TypeBasedCorrectionService parsedToInfoDbStdzService = new TypeBasedCorrectionService("after parsing");
		CorrectionDefaults.registerAllDefaultNestedBeansRetrievers(parsedToInfoDbStdzService);
		parsedToInfoDbStdzService.registerCorrector(Series.class, new SeriesNameCorrector(Pattern.compile("Scandal", Pattern.CASE_INSENSITIVE), "Scandal (US)", ImmutableList.of(), "Scandal"));
		parsedToInfoDbStdzService.registerCorrector(Series.class,
				new SeriesNameCorrector(Pattern.compile("Last Man Standing", Pattern.CASE_INSENSITIVE), "Last Man Standing (US)", ImmutableList.of(), "Last Man Standing"));
		SubCentralDe.registerSubtitleLanguageCorrectors(parsedToInfoDbStdzService);

		final TypeBasedCorrectionService infoDbToCustomStdzService = new TypeBasedCorrectionService("after infoDb");
		CorrectionDefaults.registerAllDefaultNestedBeansRetrievers(infoDbToCustomStdzService);
		infoDbToCustomStdzService.registerCorrector(Series.class, new SeriesNameCorrector(Pattern.compile("Good\\W+Wife", Pattern.CASE_INSENSITIVE), "The Good Wife", ImmutableList.of(), null));

		TimeUtil.printDurationMillisDouble("Initialization", totalStart);

		log.debug(watchFolder);
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(watchFolder))
		{
			for (Path path : directoryStream)
			{
				Path fileName = path.getFileName();
				if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && fileName.toString().toLowerCase(Locale.getDefault()).endsWith(".srt"))
				{
					long startTotalFile = System.nanoTime();
					try
					{
						System.out.println(fileName);
						String name = fileName.toString().substring(0, fileName.toString().length() - 4);
						System.out.println(name);

						System.out.println("Parsing ... ");
						long start = System.nanoTime();
						Object parsed = ps.parse(name);
						TimeUtil.printDurationMillisDouble("Parsing", start);
						System.out.println(parsed);

						System.out.println("Naming ...");
						start = System.nanoTime();
						String nameOfParsed = ns.name(parsed);
						TimeUtil.printDurationMillisDouble("Naming the parsed", start);
						System.out.println(nameOfParsed);

						start = System.nanoTime();
						List<Correction> parsedChanges = parsedToInfoDbStdzService.correct(parsed);
						parsedChanges.forEach(c -> System.out.println("Changed: " + c));
						TimeUtil.printDurationMillisDouble("Standardizing parsed", start);

						if (parsed instanceof SubtitleRelease)
						{
							SubtitleRelease subAdj = (SubtitleRelease) parsed;
							Release subAdjRls = subAdj.getFirstMatchingRelease();
							System.out.println("Querying release info db ...");
							start = System.nanoTime();
							List<Release> releases = rlsInfoDb.searchByObject(subAdj.getFirstMatchingRelease().getMedia(), Release.class);
							TimeUtil.printDurationMillisDouble("Querying release info db", start);
							System.out.println("Found releases:");
							releases.forEach(r -> System.out.println(r));

							start = System.nanoTime();
							releases.forEach(r ->
							{
								List<Correction> rlsChanges = infoDbToCustomStdzService.correct(r);
								rlsChanges.forEach(c -> System.out.println("Changed: " + c));
							});
							TimeUtil.printDurationMillisDouble("Standardizing info db results", start);

							start = System.nanoTime();
							releases.forEach(r -> ReleaseUtil.enrichByParsingName(r, ps, false));
							TimeUtil.printDurationMillisDouble("Enriched by parsing", start);
							releases.forEach(r -> System.out.println(r));

							start = System.nanoTime();

							List<Release> filteredReleases = releases.stream()
									.filter(NamingUtil.filterByNestedName(subAdjRls, (Release rls) -> rls.getMedia(), mediaNsForFiltering, NamingUtil.getDefaultParameterGenerator()))
									.filter(ReleaseUtil.filterByTags(subAdjRls.getTags(), ImmutableList.of()))
									.filter(ReleaseUtil.filterByGroup(subAdjRls.getGroup(), false))
									.collect(Collectors.toList());
							TimeUtil.printDurationMillisDouble("Filtering found releases", start);
							filteredReleases.forEach(r -> System.out.println(r));

							start = System.nanoTime();
							Map<Release, CompatibilityInfo> compatibleRlss = new HashMap<>();
							for (Release rls : filteredReleases)
							{
								Map<Release, CompatibilityInfo> rlss = compService.findCompatibles(rls, releases);
								compatibleRlss.putAll(rlss);
							}
							TimeUtil.printDurationMillisDouble("Build compatibilities", start);
							compatibleRlss.entrySet().forEach(e -> System.out.println(e));

							Set<Release> allMatchingRlss = new HashSet<>();
							allMatchingRlss.addAll(filteredReleases);
							allMatchingRlss.addAll(compatibleRlss.keySet());

							start = System.nanoTime();
							Subtitle convertedSub = new Subtitle();
							convertedSub.setMedia(subAdj.getFirstSubtitle().getMedia());
							convertedSub.setLanguage(subAdj.getFirstSubtitle().getLanguage());
							convertedSub.setGroup(subAdj.getFirstSubtitle().getGroup());
							convertedSub.setSource(subAdj.getFirstSubtitle().getSource());
							SubtitleRelease convertedAdj = new SubtitleRelease(convertedSub, allMatchingRlss);
							convertedAdj.setHearingImpaired(subAdj.isHearingImpaired());
							TimeUtil.printDurationMillisDouble("Converting releases", start);
							for (Release matchingRls : convertedAdj.getMatchingReleases())
							{
								start = System.nanoTime();
								String newName = ns.name(convertedAdj, ImmutableMap.of(SubtitleReleaseNamer.PARAM_RELEASE, matchingRls, ReleaseNamer.PARAM_PREFER_NAME, Boolean.TRUE));
								TimeUtil.printDurationMillisDouble("Naming", start);
								System.out.println("New name:");
								System.out.println(newName);

								System.out.println("Copying ...");
								start = System.nanoTime();
								Path voDir = Files.createDirectories(path.resolveSibling("!VO"));
								Path newPath = Files.copy(path, voDir.resolve(newName + ".srt"), StandardCopyOption.REPLACE_EXISTING);
								TimeUtil.printDurationMillisDouble("Copying", start);
								System.out.println("Raring ...");
								Path rarTarget = voDir.resolve(newName + ".rar");
								System.out.println(rarTarget);
								start = System.nanoTime();
								System.out.println(packager.pack(newPath, rarTarget, packCfg));
								TimeUtil.printDurationMillisDouble("Raring", start);
							}
						}
					}
					catch (RuntimeException e)
					{
						System.err.println("Exception while processing " + path);
						e.printStackTrace();
					}
					TimeUtil.printDurationMillisDouble("Processing a file", startTotalFile);
					System.out.println();
					System.out.println();
				}
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		TimeUtil.printDurationMillisDouble("total", totalStart);
	}
}