package de.subcentral.support.winrar;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import de.subcentral.support.winrar.WinRar.LocateStrategy;

public class WinRarUnpackPlayground
{
	public static void main(String[] args) throws IOException, InterruptedException, TimeoutException
	{
		WinRarPackager packager = WinRar.getPackager(LocateStrategy.RESOURCE);
		Path archive = Paths.get("C:\\Users\\mhertram\\Downloads\\Lost S01 Part 1.zip");
		packager.unpack(archive, archive.getParent());
	}

}
