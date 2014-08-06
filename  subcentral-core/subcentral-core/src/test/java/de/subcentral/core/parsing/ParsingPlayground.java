package de.subcentral.core.parsing;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import de.subcentral.core.lookup.Lookup;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NamingStandards;
import de.subcentral.impl.addic7ed.Addic7ed;
import de.subcentral.impl.orlydb.OrlyDbLookup;
import de.subcentral.impl.scene.Scene;

public class ParsingPlayground
{
	public static void main(String[] args)
	{
		final SimpleParsingService ps = new SimpleParsingService();
		List<Parser<?>> parsers = new ArrayList<>();
		parsers.addAll(Addic7ed.getParsers());
		parsers.addAll(Scene.getParsers());
		ps.setParsers(parsers);
		final NamingService ns = NamingStandards.NAMING_SERVICE;
		final Lookup<Release, ?> lookup = new OrlyDbLookup();

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
						Object parsed = ps.parse(name);
						System.out.println("Parsed to ... ");
						System.out.println(parsed);
						String nameOfParsed = ns.name(parsed);
						System.out.println(nameOfParsed);
						// System.out.println("Looked up ...");
						// lookup.createQueryFromEntity(parsed).execute().forEach(r -> System.out.println(r));
						System.out.println();
						System.out.println();

					}
					catch (Exception e)
					{
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
