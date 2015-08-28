package de.subcentral.support.winrar;

import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;

class UnixWinRarPackager extends WinRarPackager
{
	UnixWinRarPackager(UnixWinRar winRar, LocateStrategy locateStrategy, Path rarExecutable)
	{
		super(winRar, locateStrategy, rarExecutable);
	}

	@Override
	protected String determineRarExecutableResourceFilename()
	{
		if (SystemUtils.IS_OS_LINUX)
		{
			if (SystemUtils.OS_ARCH.contains("64"))
			{
				return "rar_5.10_linux_x64";
			}
			else
			{
				return "rar_5.10_linux_x32";
			}
		}
		else if (SystemUtils.IS_OS_MAC_OSX)
		{
			return "rar_5.10_macosx";
		}
		else if (SystemUtils.IS_OS_FREE_BSD)
		{
			return "rar_5.10_freebsd";
		}
		// default
		return "rar_5.10_linux_x32";
	}

	@Override
	protected boolean isRecyclingAvailable()
	{
		return false;
	}
}
