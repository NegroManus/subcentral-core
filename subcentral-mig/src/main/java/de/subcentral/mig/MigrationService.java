package de.subcentral.mig;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeoutException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Resources;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import de.subcentral.core.file.subtitle.SubRip;
import de.subcentral.core.file.subtitle.SubtitleContent;
import de.subcentral.core.file.subtitle.SubtitleFileFormat;
import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.ContributionUtil;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.SubtitleVariant;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.ParsingUtil;
import de.subcentral.core.util.IOUtil;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.releasescene.ReleaseScene;
import de.subcentral.support.subcentralde.SubCentralApi;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.subcentralde.SubCentralHttpApi;
import de.subcentral.support.winrar.WinRar;
import de.subcentral.support.winrar.WinRarPackager;

public class MigrationService
{
	private static final Logger log = LogManager.getLogger(MigrationService.class);

	private final WinRarPackager		winrar				= WinRar.getInstance().getPackager();
	private final SubCentralApi			scApi				= new SubCentralHttpApi();
	private final List<ParsingService>	subParsingServices	= ImmutableList.of(SubCentralDe.getParsingService(), Addic7edCom.getParsingService());
	private final List<ParsingService>	rlsParsingServices	= ImmutableList.of(ReleaseScene.getParsingService());
	private final ContributionParser	contributionParser;

	/**
	 * Resources.getResource("de/subcentral/mig/migration-settings.xml")
	 * 
	 * @param settingsFile
	 * @throws ConfigurationException
	 */
	public MigrationService(URL settingsFile) throws ConfigurationException
	{
		MigrationSettings settings = MigrationSettings.INSTANCE;
		settings.load(settingsFile);

		contributionParser = initContributionParser(settings);
	}

	private ContributionParser initContributionParser(MigrationSettings settings)
	{
		ContributionParser parser = new ContributionParser();
		parser.setContributionPatterns(settings.contributionPatternsProperty());
		parser.setContributorPatterns(settings.contributorPatternsProperty());
		parser.setContributionTypePatterns(settings.contributionTypePatternsProperty());
		parser.setIrrelevantPatterns(settings.irrelevantPatternsProperty());
		parser.setStandardizingService(settings.getStandardizingService());
		return parser;
	}

	public void downloadSubtitles(List<Integer> attachmentIds, Path dir) throws IOException
	{
		for (Integer attachmentId : attachmentIds)
		{
			scApi.downloadAttachment(attachmentId, dir);
		}
	}

	public void unpackSubtitles(Path srcDir, Path targetDir) throws IOException, InterruptedException, TimeoutException
	{
		// create output directory if not exists
		Files.createDirectories(targetDir);

		copyAllSrtFiles(srcDir, targetDir);

		unpackAllRecursively(srcDir, targetDir, winrar);
	}

	public List<SubFile> parseSubtitles(Path dir) throws IOException
	{
		return parseAll(dir, subParsingServices, rlsParsingServices, contributionParser);
	}

	/*
	 * private methods
	 * 
	 * 
	 */
	private static void copyAllSrtFiles(Path srcDir, Path targetDir) throws IOException
	{
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDir))
		{
			for (Path file : stream)
			{
				String ext = com.google.common.io.Files.getFileExtension(file.getFileName().toString());
				if ("srt".equals(ext))
				{
					Files.copy(file, targetDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	private static void unpackAllRecursively(Path srcDir, Path targetDir, WinRarPackager winrar) throws IOException, InterruptedException, TimeoutException
	{
		// extract all from srcDir to targetDir
		unpackAll(srcDir, targetDir, false, winrar);
		// extract all archives in targetDir and delete them afterwards
		// (necessary if there were archives in the archives that were extracted to the srcDir)
		boolean unpackedFiles = true;
		while (unpackedFiles)
		{
			unpackedFiles = unpackAll(targetDir, targetDir, true, winrar);
		}
	}

	private static boolean unpackAll(Path srcDir, Path targetDir, boolean deleteArchiveAfterUnpacking, WinRarPackager winrar) throws IOException, InterruptedException, TimeoutException
	{
		boolean unpackedFiles = false;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDir))
		{
			for (Path file : stream)
			{
				if (unpack(file, targetDir, deleteArchiveAfterUnpacking, winrar))
				{
					unpackedFiles = true;
				}
			}
		}
		return unpackedFiles;
	}

	private static boolean unpack(Path file, Path targetDir, boolean deleteArchiveAfterUnpacking, WinRarPackager winrar) throws IOException, InterruptedException, TimeoutException
	{
		if (Files.isRegularFile(file))
		{
			String fileExt = com.google.common.io.Files.getFileExtension(file.toString());
			if ("zip".equalsIgnoreCase(fileExt))
			{
				IOUtil.unzip(file, targetDir, true);
				if (deleteArchiveAfterUnpacking)
				{
					Files.delete(file);
				}
				return true;
			}
			else if ("rar".equalsIgnoreCase(fileExt))
			{
				winrar.unpack(file, targetDir);
				if (deleteArchiveAfterUnpacking)
				{
					Files.delete(file);
				}
				return true;
			}
		}
		return false;
	}

	private static List<SubFile> parseAll(Path dir, List<ParsingService> subParsingServices, List<ParsingService> rlsParsingServices, ContributionParser contributionParser) throws IOException
	{
		Map<Long, SubFile> checkSums = new HashMap<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir))
		{
			for (Path file : stream)
			{
				HashCode hash = com.google.common.io.Files.hash(file.toFile(), Hashing.md5());
				long hashAsLong = hash.asLong();
				String filenameWithoutExt = com.google.common.io.Files.getNameWithoutExtension(file.getFileName().toString());
				SubtitleVariant subAdj = ParsingUtil.parse(filenameWithoutExt, SubtitleVariant.class, subParsingServices);
				if (subAdj == null)
				{
					Release rls = ParsingUtil.parse(filenameWithoutExt, Release.class, rlsParsingServices);
					if (rls != null)
					{
						subAdj = SubtitleVariant.create(rls, null, null);
					}
				}

				log.debug("Parsed filename {} to {}", filenameWithoutExt, subAdj);
				SubFile newSubFile = new SubFile(subAdj, file);
				checkSums.merge(hashAsLong, newSubFile, ((SubFile oldValue, SubFile newValue) -> oldValue.updateWithMatchingRelease(newValue)));
			}
		}

		// parse content
		for (SubFile sub : checkSums.values())
		{
			CharsetDetector csDetector = new CharsetDetector();
			Path file = sub.getFiles().iterator().next();
			byte[] bytes = Files.readAllBytes(file);
			csDetector.setText(bytes);
			CharsetMatch match = csDetector.detect();
			log.trace("Analyzed {}: charset={}, language={}, confidence={}", file, match.getName(), match.getLanguage(), match.getConfidence());

			SubtitleFileFormat format = SubRip.INSTANCE;

			SubtitleContent data = format.read(bytes, Charset.forName(match.getName()));
			log.debug("Parsed content of {} with charset {} and format {} to SubtitleFile ({} items)", file, match.getName(), format.getName(), data.getItems().size());
			sub.updateWithData(data);

			List<Contribution> contributions = contributionParser.parse(data);
			log.debug("Parsed contributions:");
			for (Map.Entry<String, Collection<Contribution>> entry : ContributionUtil.groupByType(contributions).asMap().entrySet())
			{
				StringJoiner contributors = new StringJoiner(", ");
				for (Contribution c : entry.getValue())
				{
					contributors.add(c.getContributor().getName());
				}
				log.debug("{}: {}", entry.getKey().isEmpty() ? "<unspecified>" : entry.getKey(), contributors.toString());
			}
			sub.updateWithContributions(contributions);
		}

		List<SubFile> parsedSubtitles = new ArrayList<>(checkSums.values());
		parsedSubtitles.sort(null);
		return parsedSubtitles;
	}

	public static void main(String[] args) throws ConfigurationException, IOException
	{
		Path dir = Paths.get("C:\\Users\\mhertram\\Downloads\\!sc-target");
		URL settingsFile = Resources.getResource("de/subcentral/mig/migration-settings.xml");
		MigrationService service = new MigrationService(settingsFile);
		List<SubFile> files = service.parseSubtitles(dir);
		for (SubFile file : files)
		{
			System.out.println(file);
			System.out.println();
		}
	}
}
