package de.subcentral.core.io.winrar;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.subcentral.core.io.winrar.WinRar.RarExeLocation;
import de.subcentral.core.io.winrar.WinRarPackConfig.CompressionMethod;

public class WinRarPlayGround
{
	public static void main(String[] args) throws Exception
	{
		Path src = Paths.get("C:\\Users\\mhertram\\Downloads\\!VO\\Dallas.2012.S03E10.HDTV.x264-LOL.VO.srt");
		Path target = Paths.get("C:\\Users\\mhertram\\Downloads\\!VO\\Dallas.2012.S03E10.HDTV.x264-LOL.VO.rar");
		WinRarPackager packer = WinRar.getPackager(RarExeLocation.AUTO_LOCATE, null);

		WinRarPackConfig cfg = new WinRarPackConfig();
		cfg.setDeleteSource(false);
		cfg.setReplaceTarget(true);
		cfg.setCompressionMethod(CompressionMethod.BEST);
		WinRarPackResult result = packer.pack(src, target, cfg);
		System.out.println(result);
	}
}
