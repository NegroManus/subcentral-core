package de.subcentral.support.winrar;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.winrar.WinRar.LocateStrategy;
import de.subcentral.support.winrar.WinRarPackConfig.CompressionMethod;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.support.winrar.WinRarPackConfig.OverwriteMode;

public class WinRarPlayGround
{
	public static void main(String[] args) throws Exception
	{
		WinRarPackConfig cfg = new WinRarPackConfig();
		cfg.setTimeout(15, TimeUnit.SECONDS);
		cfg.setSourceDeletionMode(DeletionMode.KEEP);
		cfg.setTargetOverwriteMode(OverwriteMode.REPLACE);
		cfg.setCompressionMethod(CompressionMethod.NORMAL);
		Path src = Paths.get("C:\\Users\\mhertram\\Downloads\\Bescheinigung Austritt aus der evangelischen Kirchen 09-2014.pdf");
		Path target = Paths.get("C:\\Users\\mhertram\\Downloads\\Bescheinigung Austritt aus der evangelischen Kirchen 09-2014.NORMAL.rar");

		long start = System.nanoTime();
		WinRarPackager packer = WinRar.getPackager(LocateStrategy.RESOURCE);
		WinRarPackResult result = packer.pack(src, target, cfg);
		TimeUtil.printDurationMillis("Packaging", start);
		System.out.println(result);

		// LOCATE: 365-380
		// RESOURCE: 350-370ms
		// SPECIFY - WinRar.exe: 390-400
		// SPECIFY - Rar.exe: 360-370

	}
}
