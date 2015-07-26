package de.subcentral.mig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

import de.subcentral.core.file.subtitle.SubRip;
import de.subcentral.core.file.subtitle.SubtitleFile;
import de.subcentral.core.file.subtitle.SubtitleFileFormat;
import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.ContributionUtil;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.ParsingUtil;
import de.subcentral.core.util.IOUtil;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.subcentralde.SubCentralDe;

public class Migration
{
	private static final Logger	log		= LogManager.getLogger(Migration.class);
	private static final SubRip	subRip	= SubtitleFileFormat.SUBRIP;

	private static final ContributionParser		CONTRIBUTION_PARSER	= initContributionParser();
	private static final List<ParsingService>	PARSING_SERVICES	= initParsingServices();

	private static ContributionParser initContributionParser()
	{
		try
		{
			MigrationSettings settings = MigrationSettings.INSTANCE;

			settings.load(Resources.getResource("de/subcentral/mig/migration-settings.xml"));

			ContributionParser parser = new ContributionParser();
			parser.setContributionPatterns(settings.contributionPatternsProperty());
			parser.setContributorPatterns(settings.contributorPatternsProperty());
			parser.setContributionTypePatterns(settings.contributionTypePatternsProperty());
			parser.setIrrelevantPatterns(settings.irrelevantPatternsProperty());
			parser.setStandardizingService(settings.getStandardizingService());
			return parser;
		}
		catch (ConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private static List<ParsingService> initParsingServices()
	{
		ImmutableList.Builder<ParsingService> services = ImmutableList.builder();
		services.add(Addic7edCom.getParsingService());
		services.add(SubCentralDe.getParsingService());
		return services.build();
	}

	public static void main(String[] args) throws IOException
	{
		// "D:\\Downloads\\!sc-target"
		// "C:\\Users\\mhertram\\Downloads\\!sc-target"
		Path srcDir = Paths.get("D:\\Downloads\\!sc-target");

		long startTotal = System.nanoTime();
		List<Path> files = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDir))
		{
			stream.forEach(file -> files.add(file));
		}
		List<SubtitleAdjustment> parsed = files.parallelStream().map(Migration::processFile).collect(Collectors.toList());
		double duration = TimeUtil.durationMillis(startTotal);
		log.info("Processed {} files in {} ms ({} ms per file}", files.size(), duration, (duration / files.size()));
	}

	private static SubtitleAdjustment processFile(Path file)
	{
		try
		{
			long startSingle = System.nanoTime();
			log.info("Processing {}", file);
			String name = IOUtil.splitIntoFilenameAndExtension(file.getFileName().toString())[0];
			SubtitleAdjustment metadata = ParsingUtil.parse(name, SubtitleAdjustment.class, PARSING_SERVICES);
			log.info("Parsed metadata: {}", metadata);
			SubtitleFile data;

			data = subRip.read(file, Charset.forName("Cp1252"));

			log.info("Read {} items", data.getItems().size());
			List<Contribution> contributions = parseContributions(data);
			metadata.setContributions(contributions);
			log.info("Processed {} in {} ms", file, TimeUtil.durationMillis(startSingle));
			log.info("---------------------");
			log.info("---------------------");
			return metadata;
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private static List<Contribution> parseContributions(SubtitleFile data)
	{
		List<Contribution> contributions = CONTRIBUTION_PARSER.parse(data);
		log.info("Parsed contributions:");
		for (Map.Entry<String, Collection<Contribution>> entry : ContributionUtil.groupByType(contributions).asMap().entrySet())
		{
			StringJoiner contributors = new StringJoiner(", ");
			for (Contribution c : entry.getValue())
			{
				contributors.add(c.getContributor().getName());
			}
			log.info("{}: {}", entry.getKey().isEmpty() ? "<unspecified>" : entry.getKey(), contributors.toString());
		}
		return contributions;
	}
}
