package de.subcentral.core.file.subtitle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.file.subtitle.ContributionParser.ContributionTypePattern;
import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.util.TimeUtil;

public class ContributionParserPlayground
{
	private static final ContributionParser	CONTRIBUTION_PARSER	= initContributionParser();

	private static ContributionParser initContributionParser()
	{
		ImmutableList.Builder<ContributionTypePattern> patterns = ImmutableList.builder();
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Übersetzung|Übersetzung|Übersetzer|Übersetzt|Subbed)\\b(\\s+\\b(von|by)\\b)?",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), Subtitle.CONTRIBUTION_TYPE_TRANSLATION));
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Korrektur|Korrektur|Korrekturleser|Korrigiert|Revised)\\b(\\s+\\b(von|by)\\b)?",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
				Subtitle.CONTRIBUTION_TYPE_REVISION));
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Anpassung|Anpassung|Anpasser|Angepasst|Adjusted)\\b(\\s+(von|by)\\b)?",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), SubtitleAdjustment.CONTRIBUTION_TYPE_ADJUSTMENT));
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Timings|Sync|Synchronisation|Synced)\\b(\\s+\\b(von|by)\\b)?",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), Subtitle.CONTRIBUTION_TYPE_TIMINGS));
		// nicht nur "VO", weil das zu oft vorkommt. Daher "VO von"
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(VO von|VO by|Transcript|Transkript)\\b(\\s+\\b(von|by)\\b)?",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), Subtitle.CONTRIBUTION_TYPE_TRANSCRIPT));
		patterns.add(new ContributionTypePattern(Pattern.compile("(\\b(Special Thanks to|Special Thx to)\\b|Untertitel:)", Pattern.CASE_INSENSITIVE
				| Pattern.UNICODE_CASE), null));

		ImmutableSet.Builder<String> knownContributors = ImmutableSet.builder();

		ImmutableSet.Builder<String> knownNonContributors = ImmutableSet.builder();
		knownNonContributors.add("und");
		knownNonContributors.add("and");
		knownNonContributors.add("from");

		return new ContributionParser(patterns.build(), knownContributors.build(), knownNonContributors.build());
	}

	public static void main(String[] args) throws IOException
	{
		// "D:\\Downloads\\!sc-target"
		// "C:\\Users\\mhertram\\Downloads\\!sc-target"
		Path srcDir = Paths.get("D:\\Downloads\\!sc-target");
		SubRip subRip = new SubRip();
		Consumer<SubtitleFile> sink = ContributionParserPlayground::parseContributions;

		long startTotal = System.nanoTime();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDir))
		{
			for (Path file : stream)
			{
				long startRead = System.nanoTime();
				// System.out.println("Reading " + file);
				SubtitleFile data = subRip.read(file, StandardCharsets.ISO_8859_1);
				sink.accept(data);
				TimeUtil.printDurationMillis("reading one", startRead);
			}
		}
		TimeUtil.printDurationMillis("reading all", startTotal);
	}

	private static void parseContributions(SubtitleFile data)
	{
		System.out.println(data);
		Set<Contribution> contributions = CONTRIBUTION_PARSER.parse(data);
		System.out.println(contributions);
	}
}
