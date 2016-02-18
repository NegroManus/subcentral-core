package de.subcentral.mig;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.parse.MultiParsingService;
import de.subcentral.core.parse.ParsingService;
import de.subcentral.core.util.IOUtil;
import de.subcentral.mig.process.old.SubFile;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.releasescene.ReleaseScene;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.winrar.WinRar;
import de.subcentral.support.winrar.WinRarPackager;

public class ProbeFilePlayground
{
	private static final WinRarPackager	WINRAR				= WinRar.getInstance().getPackager();

	private static final ParsingService	SUB_PARSING_SERVICE	= new MultiParsingService("sub", SubCentralDe.getParsingService(), Addic7edCom.getParsingService());

	public static void main(String[] args) throws IOException, InterruptedException, TimeoutException
	{
		Path srcDir = Paths.get("C:\\Users\\mhertram\\Downloads\\!sc-src");
		Path targetDir = Paths.get("C:\\Users\\mhertram\\Downloads\\!sc-target");
		// C:\\Users\\mhertram\\Downloads
		// D:\\Downloads

		// create output directory if not exists
		Files.createDirectories(targetDir);

		copyAllSrtFiles(srcDir, targetDir);

		unpackAllRecursively(srcDir, targetDir);

		parseAll(targetDir);
	}

	private static void copyAllSrtFiles(Path srcDir, Path targetDir) throws IOException
	{
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDir))
		{
			for (Path file : stream)
			{
				String ext = com.google.common.io.Files.getFileExtension(file.getFileName().toString());
				if ("srt".equals(ext))
				{
					Files.copy(file, targetDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	private static void unpackAllRecursively(Path srcDir, Path targetDir) throws IOException, InterruptedException, TimeoutException
	{
		// extract all from srcDir to targetDir
		unpackAll(srcDir, targetDir, false);
		// extract all archives in targetDir and delete them afterwards (necessary if there were archives in the archives in the srcDir)
		boolean unpackedFiles = true;
		while (unpackedFiles)
		{
			unpackedFiles = unpackAll(targetDir, targetDir, true);
		}
	}

	private static boolean unpackAll(Path srcDir, Path targetDir, boolean deleteArchiveAfterUnpacking) throws IOException, InterruptedException, TimeoutException
	{
		boolean unpackedFiles = false;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(srcDir))
		{
			for (Path file : stream)
			{
				if (unpack(file, targetDir, deleteArchiveAfterUnpacking))
				{
					unpackedFiles = true;
				}
			}
		}
		return unpackedFiles;
	}

	private static boolean unpack(Path file, Path targetDir, boolean deleteArchiveAfterUnpacking) throws IOException, InterruptedException, TimeoutException
	{
		if (Files.isRegularFile(file))
		{
			String fileExt = com.google.common.io.Files.getFileExtension(file.toString());
			if ("zip".equalsIgnoreCase(fileExt))
			{
				IOUtil.unzip(file, targetDir, true);
				if (deleteArchiveAfterUnpacking)
				{
					Files.delete(file);
				}
				return true;
			}
			else if ("rar".equalsIgnoreCase(fileExt))
			{
				WINRAR.unpack(file, targetDir);
				if (deleteArchiveAfterUnpacking)
				{
					Files.delete(file);
				}
				return true;
			}
		}
		return false;
	}

	private static void parseAll(Path dir) throws IOException
	{
		Map<Long, SubFile> checkSums = new HashMap<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir))
		{
			for (Path file : stream)
			{
				HashCode hash = com.google.common.io.Files.hash(file.toFile(), Hashing.md5());
				long hashAsLong = hash.asLong();
				String filenameWithoutExt = com.google.common.io.Files.getNameWithoutExtension(file.getFileName().toString());
				SubtitleRelease subAdj = SUB_PARSING_SERVICE.parse(filenameWithoutExt, SubtitleRelease.class);
				if (subAdj == null)
				{
					Release rls = ReleaseScene.getParsingService().parse(filenameWithoutExt, Release.class);
					if (rls != null)
					{
						subAdj = SubtitleRelease.create(rls, null, null);
					}
				}

				SubFile newSubFile = new SubFile(subAdj, file);
				checkSums.merge(hashAsLong, newSubFile, ((SubFile oldValue, SubFile newValue) -> oldValue.updateWithMatchingRelease(newValue)));
			}
		}

		for (Entry<Long, SubFile> entry : checkSums.entrySet())
		{
			System.out.println(entry.getKey() + " =>");
			System.out.println(entry.getValue().getFiles().size() + " files: " + entry.getValue().getFiles());
			System.out.println(entry.getValue().getSubtitleMetadata());
			System.out.println();
		}
	}
}
