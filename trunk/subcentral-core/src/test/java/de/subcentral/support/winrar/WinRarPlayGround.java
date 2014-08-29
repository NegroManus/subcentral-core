package de.subcentral.support.winrar;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.winrar.WinRar.RarExeLocation;
import de.subcentral.support.winrar.WinRarPackConfig.CompressionMethod;

public class WinRarPlayGround
{
	public static void main(String[] args) throws Exception
	{
		WinRarPackConfig cfg = new WinRarPackConfig();
		cfg.setTimeout(15);
		cfg.setTimeoutUnit(TimeUnit.SECONDS);
		cfg.setDeleteSource(false);
		cfg.setReplaceTarget(false);
		cfg.setCompressionMethod(CompressionMethod.BEST);
		Path src = Paths.get("C:\\Users\\mhertram\\Downloads\\2012_01rechnung_5612261167.pdf");
		Path target = Paths.get("C:\\Users\\mhertram\\Downloads\\2012_01rechnung_5612261167.rar");

		long start = System.nanoTime();
		WinRarPackager packer = WinRar.getPackager(RarExeLocation.RESOURCE);
		WinRarPackResult result = packer.pack(src, target, cfg);
		TimeUtil.printDurationMillis(start);
		System.out.println(result);

		// LOCATE: 365-380
		// RESOURCE: 350-370ms
		// SPECIFY - WinRar.exe: 390-400
		// SPECIFY - Rar.exe: 360-370

	}
}