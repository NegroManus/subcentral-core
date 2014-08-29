package de.subcentral.support.winrar;

import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;

public class WinRar
{
	public static enum RarExeLocation
	{
		SPECIFY, LOCATE, RESOURCE;
	}

	public static final WinRarPackager getPackager(RarExeLocation rarExeLocation)
	{
		return getPackager(rarExeLocation, null);
	}

	public static final WinRarPackager getPackager(RarExeLocation rarExeLocation, Path rarExe)
	{
		if (SystemUtils.IS_OS_WINDOWS)
		{
			return new WindowsWinRarPackager(rarExeLocation, rarExe);
		}
		else if (SystemUtils.IS_OS_UNIX)
		{
			return new UnixWinRarPackager(rarExeLocation, rarExe);
		}
		throw new IllegalStateException("Operating system " + SystemUtils.OS_NAME + " " + SystemUtils.OS_VERSION + " " + SystemUtils.OS_ARCH
				+ " not supported. Only Windows and Unix like systems are supported.");
	}

	private WinRar()
	{
		// utility method
	}
}
