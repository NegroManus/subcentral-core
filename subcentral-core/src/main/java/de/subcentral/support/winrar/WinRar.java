package de.subcentral.support.winrar;

import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;

public class WinRar
{
	public static enum LocateStrategy
	{
		SPECIFY, LOCATE, RESOURCE;
	}

	public static final String getRarExecutableFilename()
	{
		if (SystemUtils.IS_OS_WINDOWS)
		{
			return WindowsWinRarPackager.RAR_EXECUTABLE_FILENAME;
		}
		else if (SystemUtils.IS_OS_UNIX)
		{
			return UnixWinRarPackager.RAR_EXECUTABLE_FILENAME;
		}
		return null;
	}

	public static final WinRarPackager getPackager(LocateStrategy locateStrategy)
	{
		return getPackager(locateStrategy, null);
	}

	public static final WinRarPackager getPackager(LocateStrategy locateStrategy, Path rarExe)
	{
		if (SystemUtils.IS_OS_WINDOWS)
		{
			return new WindowsWinRarPackager(locateStrategy, rarExe);
		}
		else if (SystemUtils.IS_OS_UNIX)
		{
			return new UnixWinRarPackager(locateStrategy, rarExe);
		}
		throw new IllegalStateException("Operating system " + SystemUtils.OS_NAME + " " + SystemUtils.OS_VERSION + " " + SystemUtils.OS_ARCH
				+ " not supported. Only Windows and Unix like systems are supported.");
	}

	private WinRar()
	{
		// utility method
	}
}
