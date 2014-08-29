package de.subcentral.support.winrar;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.winrar.WinRar.RarExeLocation;
import de.subcentral.support.winrar.WinRarPackConfig.CompressionMethod;

public class WinRarPlayGround
{
	public static void main(String[] args) throws Exception
	{
		WinRarPackConfig cfg = new WinRarPackConfig();
		cfg.setDeleteSource(false);
		cfg.setReplaceTarget(true);
		cfg.setCompressionMethod(CompressionMethod.BEST);
		Path src = Paths.get("C:\\Users\\mhertram\\Downloads\\!VO\\Dallas.2012.S03E10.HDTV.x264-LOL.VO.srt");
		Path target = Paths.get("C:\\Users\\mhertram\\Downloads\\!VO\\Dallas.2012.S03E10.HDTV.x264-LOL.VO.rar");

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
