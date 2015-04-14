package de.subcentral.support.winrar;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProbeFilePlayground
{
	public static void main(String[] args) throws IOException
	{
		Path srcDir = Paths.get("D:\\Downloads");

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDir))
		{
			for (Path file : stream)
			{
				String contentType = Files.probeContentType(file);
				System.out.println(file + ": " + contentType);
			}
		}
	}
}
