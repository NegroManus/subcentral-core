package de.subcentral.support.winrar;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.subcentral.core.util.IOUtil;

public class UnzipPlayground
{
	public static void main(String[] args) throws IOException
	{
		Path targetDir = Paths.get("C:\\Users\\mhertram\\Downloads\\!sc-target");
		Path archive = Paths.get("C:\\Users\\mhertram\\Downloads\\!sc-src\\Lost S01 Part 1.zip");
		IOUtil.unzip(archive, targetDir, true);
	}

}
