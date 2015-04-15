package de.subcentral.support.winrar;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import de.subcentral.support.winrar.WinRar.LocateStrategy;

public class ProbeFilePlayground
{
	public static void main(String[] args) throws IOException, InterruptedException, TimeoutException
	{
		WinRarPackager packager = WinRar.getPackager(LocateStrategy.RESOURCE);
		Path srcDir = Paths.get("C:\\Users\\mhertram\\Downloads");

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDir))
		{
			for (Path file : stream)
			{
				String contentType = Files.probeContentType(file);
				System.out.println(file + ": " + contentType);
				System.out.println(file + " is RAR : " + packager.validate(file));

			}
		}
	}
}
