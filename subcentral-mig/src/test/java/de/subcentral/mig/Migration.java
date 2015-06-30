package de.subcentral.mig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.configuration2.ex.ConfigurationException;

import com.google.common.io.Resources;

import de.subcentral.core.file.subtitle.SubRip;
import de.subcentral.core.file.subtitle.SubtitleFile;
import de.subcentral.core.file.subtitle.SubtitleFileFormat;
import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.util.TimeUtil;

public class Migration
{
    private static final ContributionParser CONTRIBUTION_PARSER = initContributionParser();

    private static ContributionParser initContributionParser()
    {
	try
	{
	    MigrationSettings settings = MigrationSettings.INSTANCE;

	    settings.load(Resources.getResource("de/subcentral/mig/migration-settings.xml"));

	    ContributionParser parser = new ContributionParser(settings.contributionTypePatternsProperty(),
		    settings.knownContributorsProperty(),
		    settings.knownNonContributorsProperty(),
		    settings.getStandardizingService());
	    return parser;
	}
	catch (ConfigurationException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return null;
	}
    }

    public static void main(String[] args) throws IOException
    {
	// "D:\\Downloads\\!sc-target"
	// "C:\\Users\\mhertram\\Downloads\\!sc-target"
	Path srcDir = Paths.get("C:\\Users\\mhertram\\Downloads\\!sc-target");
	SubRip subRip = SubtitleFileFormat.SUBRIP;
	Consumer<SubtitleFile> sink = Migration::parseContributions;

	long startTotal = System.nanoTime();
	try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDir))
	{
	    for (Path file : stream)
	    {
		long startRead = System.nanoTime();
		// System.out.println("Reading " + file);
		SubtitleFile data = subRip.read(file, Charset.forName("Cp1252"));
		System.out.println(file);
		sink.accept(data);
		TimeUtil.printDurationMillis("reading one", startRead);
	    }
	}
	TimeUtil.printDurationMillis("reading all", startTotal);
    }

    private static void parseContributions(SubtitleFile data)
    {
	// System.out.println(data);
	List<Contribution> contributions = CONTRIBUTION_PARSER.parse(data);
	System.out.println(contributions);
    }
}
