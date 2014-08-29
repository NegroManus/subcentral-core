package de.subcentral.core.parsing;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.lookup.Lookup;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.DelegatingNamingService;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NamingStandards;
import de.subcentral.core.naming.SubtitleAdjustmentNamer;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.addic7ed.Addic7ed;
import de.subcentral.support.scene.Scene;
import de.subcentral.support.subcentral.SubCentral;
import de.subcentral.support.winrar.WinRar;
import de.subcentral.support.winrar.WinRar.RarExeLocation;
import de.subcentral.support.winrar.WinRarPackConfig;
import de.subcentral.support.winrar.WinRarPackConfig.CompressionMethod;
import de.subcentralsupport.orlydb.OrlyDbLookup;

public class ParsingPlayground
{
	public static void main(String[] args)
	{
		System.getProperties().put("http.proxyHost", "10.206.247.65");
		System.getProperties().put("http.proxyPort", "8080");

		final SimpleParsingService ps = new SimpleParsingService();
		ImmutableListMultimap.Builder<Class<?>, Parser<?>> parsers = ImmutableListMultimap.builder();
		parsers.putAll(SubCentral.getParsers());
		parsers.putAll(Addic7ed.getParsers());
		parsers.putAll(Scene.getParsers());
		ps.setParsers(parsers.build());
		final NamingService ns = NamingStandards.NAMING_SERVICE;
		final Lookup<Release, ?> lookup = new OrlyDbLookup();

		WinRarPackConfig packCfg = new WinRarPackConfig();
		packCfg.setDeleteSource(false);
		packCfg.setReplaceTarget(true);
		packCfg.setCompressionMethod(CompressionMethod.BEST);

		Path dlFolder = Paths.get(System.getProperty("user.home"), "Downloads");
		// dlFolder = Paths.get("D:\\Downloads");
		System.out.println(dlFolder);
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dlFolder))
		{
			for (Path path : directoryStream)
			{
				Path fileName = path.getFileName();
				if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && fileName.toString().toLowerCase().endsWith(".srt"))
				{
					try
					{
						System.out.println(fileName);
						String name = fileName.toString().substring(0, fileName.toString().length() - 4);
						System.out.println(name);
						long start = System.nanoTime();
						Object parsed = ps.parse(name);

						TimeUtil.printDurationMillis(start);
						System.out.println("Parsed to ... ");
						System.out.println(parsed);
						start = System.nanoTime();
						String nameOfParsed = ns.name(parsed);
						TimeUtil.printDurationMillis(start);
						System.out.println("Named to ...");
						System.out.println(nameOfParsed);
						System.out.println("Looked up ...");
						if (parsed instanceof SubtitleAdjustment)
						{
							SubtitleAdjustment subAdj = (SubtitleAdjustment) parsed;
							Release subAdjRls = subAdj.getFirstMatchingRelease();
							start = System.nanoTime();
							List<Release> releases = lookup.createQueryFromEntity(subAdj.getFirstMatchingRelease().getFirstMedia()).execute();
							TimeUtil.printDurationMillis(start);
							System.out.println("Found releases:");
							releases.forEach(r -> System.out.println(r));
							start = System.nanoTime();
							releases.forEach(r -> Releases.enrichByParsingName(r, ps, false));
							System.out.println("Parsed releases:");
							releases.forEach(r -> System.out.println(r));
							DelegatingNamingService mediaNsForFiltering = new DelegatingNamingService(NamingStandards.NAMING_SERVICE,
									NamingStandards.STANDARD_REPLACER);
							List<Release> filteredReleases = Releases.filter(releases,
									ImmutableList.of(subAdj.getFirstSubtitle().getMedia()),
									subAdjRls.getTags(),
									subAdjRls.getGroup(),
									mediaNsForFiltering);
							System.out.println("Filtered releases:");
							filteredReleases.forEach(r -> System.out.println(r));

							Subtitle convertedSub = new Subtitle();
							convertedSub.setMedia(subAdj.getFirstSubtitle().getMedia());
							convertedSub.setHearingImpaired(subAdj.getFirstSubtitle().isHearingImpaired());
							convertedSub.setLanguage(subAdj.getFirstSubtitle().getLanguage());
							SubCentral.standardizeSubtitleLanguage(convertedSub);
							SubtitleAdjustment convertedAdj = convertedSub.newAdjustment(filteredReleases);
							TimeUtil.printDurationMillis(start);
							for (Release matchingRls : filteredReleases)
							{
								String newName = ns.name(convertedAdj, ImmutableMap.of(SubtitleAdjustmentNamer.PARAM_KEY_RELEASE, matchingRls));
								System.out.println("New name:");
								System.out.println(newName);

								System.out.println("Raring");
								start = System.nanoTime();
								Path voDir = Files.createDirectories(path.resolveSibling("!VO"));
								Path rarTarget = voDir.resolve(newName + ".rar");
								System.out.println(rarTarget);
								System.out.println(WinRar.getPackager(RarExeLocation.RESOURCE).pack(path, rarTarget, packCfg));
								TimeUtil.printDurationMillis(start);
							}

						}

						System.out.println();
						System.out.println();

					}
					catch (Exception e)
					{
						System.err.println("Exception while processing " + path);
						e.printStackTrace();
					}
				}
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
