package de.subcentral.core.parsing;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.subcentral.core.lookup.Lookup;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.NamingStandards;
import de.subcentral.impl.addic7ed.Addic7ed;
import de.subcentral.impl.orlydb.OrlyDbLookup;
import de.subcentral.impl.orlydb.OrlyDbLookupParameters;

public class ParsingPlayground
{
	public static void main(String[] args)
	{
		final ParsingService ps = Addic7ed.getAddi7edParsingService();
		final Lookup<Release, OrlyDbLookupParameters> lookup = new OrlyDbLookup();

		Path dlFolder = Paths.get(System.getProperty("user.home"), "Downloads");
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dlFolder))
		{
			for (Path path : directoryStream)
			{
				Path fileName = path.getFileName();
				if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && fileName.toString().toLowerCase().endsWith(".srt"))
				{
					System.out.println(fileName);
					String name = fileName.toString().substring(0, fileName.toString().length() - 4);
					System.out.println(name);
					SubtitleAdjustment parsed = ps.parse(name, SubtitleAdjustment.class);
					System.out.println("Parsed to ... ");
					System.out.println(parsed);
					String nameOfParsed = NamingStandards.NAMING_SERVICE.name(parsed);
					System.out.println(nameOfParsed);
					// System.out.println("Looked up ...");
					// lookup.createQuery("Psych S01E01").execute().forEach(r -> System.out.println(r));
					System.out.println();
					System.out.println();

				}
			}
		}
		catch (IOException ex)
		{}
	}
}
