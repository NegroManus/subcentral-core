package de.subcentral.support.winrar;

import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;

class WindowsWinRarPackager extends WinRarPackager
{
	WindowsWinRarPackager(WindowsWinRar winRar, LocateStrategy locateStrategy, Path rarExecutable)
	{
		super(winRar, locateStrategy, rarExecutable);
	}

	@Override
	protected String determineRarExecutableResourceFilename()
	{
		if (SystemUtils.OS_ARCH.contains("64"))
		{
			return "rar_5.10_win_x64.exe";
		}
		else
		{
			return "rar_5.10_win_x32.exe";
		}
	}

	@Override
	protected boolean isRecyclingAvailable()
	{
		return true;
	}
}
