package de.subcentral.subman.mig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.file.subtitle.SubRip;
import de.subcentral.core.file.subtitle.SubtitleFile;
import de.subcentral.core.file.subtitle.SubtitleFileFormat;
import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.standardizing.PatternStringReplacer;
import de.subcentral.core.standardizing.StringReplacer;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.subman.mig.ContributionParser.ContributionTypePattern;

public class Migration
{
	private static final ContributionParser	CONTRIBUTION_PARSER	= initContributionParser();

	private static ContributionParser initContributionParser()
	{
		ImmutableList.Builder<ContributionTypePattern> patterns = ImmutableList.builder();
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Übersetzung|Übersetzer|Übersetzt|Subbed by|Untertitel)\\b",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), Subtitle.CONTRIBUTION_TYPE_TRANSLATION));
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Korrektur|Korrekturen|Korrekturleser|Korrigiert|Revised by|Re-revised by|Überarbeitung|Überarbeitet|Corrected)\\b",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
				Subtitle.CONTRIBUTION_TYPE_REVISION));
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Anpassung|Anpasser|Angepasst|Adjusted by)\\b", Pattern.CASE_INSENSITIVE
				| Pattern.UNICODE_CASE), SubtitleAdjustment.CONTRIBUTION_TYPE_ADJUSTMENT));
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Timings|Timings|Sync|Synced|Synchro|Sync's|Syncs|Sync)\\b",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), Subtitle.CONTRIBUTION_TYPE_TIMINGS));
		// nicht nur "VO", weil das zu oft vorkommt. Daher "VO von"
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(VO von|VO by|Transcript|Subs)\\b", Pattern.CASE_INSENSITIVE
				| Pattern.UNICODE_CASE), Subtitle.CONTRIBUTION_TYPE_TRANSCRIPT));
		patterns.add(new ContributionTypePattern(Pattern.compile("\\b(Special Thx to)\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), null));

		ImmutableSet.Builder<Pattern> knownContributors = ImmutableSet.builder();
		knownContributors.add(Pattern.compile("(Randall Flagg|The old Man|JW 301|-TiLT- aka smizz|smizz aka -TiLT-|Kami Cat|Iulius Monea)",
				Pattern.CASE_INSENSITIVE));

		ImmutableSet.Builder<Pattern> knownNonContributors = ImmutableSet.builder();
		knownNonContributors.add(Pattern.compile("\\b(und|and|from|von|by)\\b", Pattern.CASE_INSENSITIVE));

		ImmutableList.Builder<Function<String, String>> contributorReplacers = ImmutableList.builder();
		contributorReplacers.add(new PatternStringReplacer(Pattern.compile("(.*)\\."), "$1", PatternStringReplacer.Mode.REPLACE_WHOLLY));
		contributorReplacers.add(new StringReplacer("Ic3m4n", "Ic3m4n™", StringReplacer.Mode.REPLACE_WHOLLY));
		contributorReplacers.add(new StringReplacer("smizz aka -TiLT-", "-TiLT- aka smizz", StringReplacer.Mode.REPLACE_WHOLLY));

		return new ContributionParser(patterns.build(), knownContributors.build(), knownNonContributors.build(), contributorReplacers.build());
	}

	// public static void main(String[] args) throws IOException
	// {
	// int attachmentId = 10527;
	// SubCentralApi service = new SubCentralApi();
	// service.login("NegroManus", "sc-don13duck-");
	// service.downloadAttachment(attachmentId, Paths.get(System.getProperty("user.home"), "Downloads", "!sc-src"));
	// // service.logout();
	// }

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
