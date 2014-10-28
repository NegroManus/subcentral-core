package de.subcentral.core.parsing;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.JavaLookup;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.lookup.Lookup;
import de.subcentral.core.model.release.Compatibility;
import de.subcentral.core.model.release.Compatibility.Scope;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.model.subtitle.Subtitles;
import de.subcentral.core.naming.DelegatingNamingService;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NamingStandards;
import de.subcentral.core.naming.SubtitleAdjustmentNamer;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.addic7ed.Addic7ed;
import de.subcentral.support.scene.Scene;
import de.subcentral.support.subcentral.SubCentral;
import de.subcentral.support.winrar.WinRar;
import de.subcentral.support.winrar.WinRar.LocateStrategy;
import de.subcentral.support.winrar.WinRarPackConfig;
import de.subcentral.support.winrar.WinRarPackConfig.CompressionMethod;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.support.winrar.WinRarPackConfig.OverwriteMode;
import de.subcentralsupport.orlydb.OrlyDbLookup;

public class ParsingPlayground
{
	private static final Logger	log	= LogManager.getLogger(ParsingPlayground.class.getName());

	/**
	 * To specify other watch folder than "<user.home>/Downloads", add argument "watchFolder="D:\Downloads".
	 * 
	 * If behind proxy, add VM args:
	 * 
	 * <pre>
	 * -Dhttp.proxyHost=10.206.247.65
	 * -Dhttp.proxyHost=10.206.247.65
	 * </pre>
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args)
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

		long totalStart = System.nanoTime();

		final SimpleParsingService ps = new SimpleParsingService("default");
		ps.getParsers().putAll(Addic7ed.getAllParsers());
		ps.getParsers().putAll(SubCentral.getAllParsers());
		ps.getParsers().putAll(Scene.getAllParsers());

		final NamingService ns = NamingStandards.getDefaultNamingService();

		Lookup<Release, ?> lookup = new OrlyDbLookup();
		DelegatingNamingService mediaNsForFiltering = new DelegatingNamingService("orlydb",
				NamingStandards.getDefaultNamingService(),
				NamingStandards.getDefaultWholeNameOperator());

		List<Compatibility> compatibilities = new ArrayList<>();
		compatibilities.add(new Compatibility(null, new Group("LOL"), Tag.list("HDTV", "x264"), new Group("DIMENSION"), Tag.list("720p",
				"HDTV",
				"x264"), Scope.IF_EXISTS, true));
		compatibilities.add(new Compatibility(null, new Group("EXCELLENCE"), Tag.list("HDTV", "x264"), new Group("REMARKABLE"), Tag.list("720p",
				"HDTV",
				"x264"), Scope.IF_EXISTS, true));

		WinRarPackConfig packCfg = new WinRarPackConfig();
		packCfg.setSourceDeletionMode(DeletionMode.KEEP);
		packCfg.setTargetOverwriteMode(OverwriteMode.REPLACE);
		packCfg.setCompressionMethod(CompressionMethod.BEST);

		TimeUtil.printDurationMillis("Initialization", totalStart);

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

						System.out.println("Parsing... ");
						long start = System.nanoTime();
						Object parsed = ps.parse(name);
						TimeUtil.printDurationMillis("Parsing", start);
						System.out.println(parsed);

						System.out.println("Naming ...");
						start = System.nanoTime();
						String nameOfParsed = ns.name(parsed);
						TimeUtil.printDurationMillis("Naming the parsed", start);
						System.out.println(nameOfParsed);

						System.out.println("Looking up ...");
						if (parsed instanceof SubtitleAdjustment)
						{
							SubtitleAdjustment subAdj = (SubtitleAdjustment) parsed;
							Release subAdjRls = subAdj.getFirstMatchingRelease();
							start = System.nanoTime();
							List<Release> releases = lookup.createQueryFromEntity(subAdj.getFirstMatchingRelease().getMedia()).execute();
							TimeUtil.printDurationMillis("Lookup", start);
							System.out.println("Found releases:");
							releases.forEach(r -> System.out.println(r));

							start = System.nanoTime();
							releases.forEach(r -> Releases.enrichByParsingName(r, ps, false));
							TimeUtil.printDurationMillis("Enriched by parsing", start);
							releases.forEach(r -> System.out.println(r));

							start = System.nanoTime();
							List<Release> filteredReleases = Releases.filter(releases,
									Subtitles.getMediaFromSubtitles(subAdj),
									subAdjRls.getTags(),
									subAdjRls.getGroup(),
									mediaNsForFiltering);
							TimeUtil.printDurationMillis("Filtering found releases", start);
							filteredReleases.forEach(r -> System.out.println(r));

							start = System.nanoTime();
							Map<Release, Compatibility> compatibleRlss = new HashMap<>();
							for (Release rls : filteredReleases)
							{
								Map<Release, Compatibility> rlss = Releases.findCompatibleReleases(rls, compatibilities, releases);
								compatibleRlss.putAll(rlss);
							}
							TimeUtil.printDurationMillis("Build compatibilities", start);
							compatibleRlss.entrySet().forEach(e -> System.out.println(e));

							List<Release> matchingRlss = new ArrayList<>();
							matchingRlss.addAll(filteredReleases);
							matchingRlss.addAll(compatibleRlss.keySet());

							start = System.nanoTime();
							Subtitle convertedSub = new Subtitle();
							convertedSub.setMedia(subAdj.getFirstSubtitle().getMedia());
							convertedSub.setHearingImpaired(subAdj.getFirstSubtitle().isHearingImpaired());
							convertedSub.setLanguage(subAdj.getFirstSubtitle().getLanguage());
							convertedSub.setGroup(subAdj.getFirstSubtitle().getGroup());
							convertedSub.setSource(subAdj.getFirstSubtitle().getSource());
							SubCentral.standardizeSubtitleLanguage(convertedSub);
							SubtitleAdjustment convertedAdj = new SubtitleAdjustment(convertedSub, matchingRlss);
							TimeUtil.printDurationMillis("Converting releases", start);
							for (Release matchingRls : matchingRlss)
							{
								start = System.nanoTime();
								String newName = ns.name(convertedAdj, ImmutableMap.of(SubtitleAdjustmentNamer.PARAM_KEY_RELEASE, matchingRls));
								TimeUtil.printDurationMillis("Naming", start);
								System.out.println("New name:");
								System.out.println(newName);

								System.out.println("Copying ...");
								start = System.nanoTime();
								Path voDir = Files.createDirectories(path.resolveSibling("!VO"));
								Path newPath = Files.copy(path, voDir.resolve(newName + ".srt"), StandardCopyOption.REPLACE_EXISTING);
								TimeUtil.printDurationMillis("Copying", start);
								System.out.println("Raring ...");
								Path rarTarget = voDir.resolve(newName + ".rar");
								System.out.println(rarTarget);
								start = System.nanoTime();
								System.out.println(WinRar.getPackager(LocateStrategy.RESOURCE).pack(newPath, rarTarget, packCfg));
								TimeUtil.printDurationMillis("Raring", start);
							}
						}
					}
					catch (RuntimeException e)
					{
						System.err.println("Exception while processing " + path);
						e.printStackTrace();
					}
					TimeUtil.printDurationMillis("Processing a file", startTotalFile);
					System.out.println();
					System.out.println();
				}
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		TimeUtil.printDurationMillis("total", totalStart);
	}

}