package de.subcentral.core.io.winrar;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;

public class WinRar
{
	public static enum RarExeLocation
	{
		EXPLICIT, AUTO_LOCATE, RESOURCE;
	}

	public static final WinRarPackager getPackager(RarExeLocation rarExeLocationSpecifier)
	{
		return getPackager(rarExeLocationSpecifier, null);
	}

	public static final WinRarPackager getPackager(RarExeLocation rarExeLocationSpecifier, Path rarExe)
	{
		if (RarExeLocation.EXPLICIT == rarExeLocationSpecifier && (rarExe == null || Files.isRegularFile(rarExe)))
		{
			throw new IllegalArgumentException("RarExeLocation was specified as EXPLICIT but rarExe was no regular file");
		}
		if (SystemUtils.IS_OS_WINDOWS)
		{
			return new WindowsWinRarPackager(rarExeLocationSpecifier, rarExe);
		}
		else if (SystemUtils.IS_OS_UNIX)
		{
			return new UnixWinRarPackager(rarExeLocationSpecifier, rarExe);
		}
		throw new IllegalStateException("Operating system " + SystemUtils.OS_NAME + " " + SystemUtils.OS_VERSION + " " + SystemUtils.OS_ARCH
				+ " not supported. Only Windows and Unix like systems are supported.");
	}

	private WinRar()
	{
		// utility method
	}
}
