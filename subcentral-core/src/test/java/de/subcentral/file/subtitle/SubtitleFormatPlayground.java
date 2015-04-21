package de.subcentral.file.subtitle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import de.subcentral.core.file.subtitle.SubRip;
import de.subcentral.core.file.subtitle.SubtitleFile;
import de.subcentral.core.util.TimeUtil;

public class SubtitleFormatPlayground
{
	public static void main(String[] args) throws IOException
	{
		// "C:\\Users\\mhertram\\Downloads\\!sc-src"
		Path srcDir = Paths.get("D:\\Downloads\\!sc-src");
		SubRip subRip = new SubRip();
		Consumer<SubtitleFile> sink = (SubtitleFile data) -> {
			// System.out.println(data);
		};

		long startTotal = System.nanoTime();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDir))
		{
			for (Path file : stream)
			{
				long startRead = System.nanoTime();
				// System.out.println("Reading " + file);
				SubtitleFile data = subRip.read(file, StandardCharsets.UTF_8);
				sink.accept(data);
				TimeUtil.printDurationMillis("reading one", startRead);
				long startWrite = System.nanoTime();
				subRip.write(data, file.resolveSibling(file.getFileName().toString() + ".backup"), StandardCharsets.UTF_8);
				TimeUtil.printDurationMillis("writing one", startWrite);
			}
		}
		TimeUtil.printDurationMillis("all", startTotal);

	}
}
