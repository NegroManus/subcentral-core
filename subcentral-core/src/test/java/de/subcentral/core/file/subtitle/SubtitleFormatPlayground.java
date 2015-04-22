package de.subcentral.core.file.subtitle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.Contributor;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.util.TimeUtil;

public class SubtitleFormatPlayground
{
	private static final List<Function<String, List<Contribution>>>	CONTRIBUTION_PARSERS	= initContributionParsers();

	private static final List<Function<String, List<Contribution>>> initContributionParsers()
	{
		ImmutableList.Builder<Function<String, List<Contribution>>> b = ImmutableList.builder();
		b.add((String text) -> {

			// normalize text
			text = text.replace("..: ", "").replace(" :..", "");

			String subberSeparator = "(,\\s*| / | und | & )";
			Splitter subberSplitter = Splitter.on(Pattern.compile(subberSeparator)).omitEmptyStrings().trimResults();
			String subberList = "[:\\s]+(.+" + subberSeparator + "?)+";

			List<Contribution> list = new ArrayList<>();
			Matcher mt = Pattern.compile("(?:Übersetzung|Übersetzt von|Übersetzer)" + subberList, Pattern.CASE_INSENSITIVE).matcher(text);
			if (mt.find())
			{
				String translatorList = mt.group(1);
				for (String translator : subberSplitter.split(translatorList))
				{
					list.add(new Contribution(new Subber(translator), Subtitle.CONTRIBUTION_TYPE_TRANSLATION));
				}
			}
			Matcher mk = Pattern.compile("(?:Korrektur|Korrigiert von|Korrekturleser)" + subberList, Pattern.CASE_INSENSITIVE).matcher(text);
			if (mk.find())
			{
				String reviserList = mk.group(1);
				for (String reviser : subberSplitter.split(reviserList))
				{
					list.add(new Contribution(new Subber(reviser), Subtitle.CONTRIBUTION_TYPE_REVISION));
				}
			}
			Matcher ma = Pattern.compile("(?:Anpassung|Angepasst von|Anpasser)" + subberList, Pattern.CASE_INSENSITIVE).matcher(text);
			if (ma.find())
			{
				String adjusterList = ma.group(1);
				for (String adjuster : subberSplitter.split(adjusterList))
				{
					list.add(new Contribution(new Subber(adjuster), SubtitleAdjustment.CONTRIBUTION_TYPE_ADJUSTMENT));
				}
			}
			return list;
		});

		return b.build();
	}

	public static void main(String[] args) throws IOException
	{

		// "C:\\Users\\mhertram\\Downloads\\!sc-src"
		Path srcDir = Paths.get("C:\\Users\\mhertram\\Downloads\\!sc-target");
		SubRip subRip = new SubRip();
		Consumer<SubtitleFile> sink = SubtitleFormatPlayground::parseContributions;

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
				// long startWrite = System.nanoTime();
				// subRip.write(data, file.resolveSibling(file.getFileName().toString() + ".backup"), StandardCharsets.UTF_8);
				// TimeUtil.printDurationMillis("writing one", startWrite);
			}
		}
		TimeUtil.printDurationMillis("all", startTotal);
	}

	private static void parseContributions(SubtitleFile data)
	{
		// System.out.println(data);
		List<Contribution> contributions = data.getItems().stream().map((Item item) -> {
			List<Contribution> list = new ArrayList<>();
			for (Function<String, List<Contribution>> parser : CONTRIBUTION_PARSERS)
			{
				list.addAll(parser.apply(item.getText()));
			}
			return list;
		}).reduce((List<Contribution> list1, List<Contribution> list2) -> {
			list1.addAll(list2);
			return list1;
		}).get();
		System.out.println(contributions);
	}

	public static class Subber implements Contributor
	{
		private final String	name;

		public Subber(String name)
		{
			this.name = name;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(Subber.class).omitNullValues().add("name", name).toString();
		}
	}

}
