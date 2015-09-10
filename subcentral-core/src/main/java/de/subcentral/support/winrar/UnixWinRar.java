package de.subcentral.support.winrar;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

public class UnixWinRar extends WinRar
{
	private static final Logger log = LogManager.getLogger(UnixWinRar.class.getName());

	private static final Path RAR_EXECUTABLE_FILENAME = Paths.get("rar");

	@Override
	public Path getRarExecutableFilename()
	{
		return RAR_EXECUTABLE_FILENAME;
	}

	@Override
	public WinRarPackager getPackager(Path rarExecutable)
	{
		return new UnixWinRarPackager(rarExecutable);
	}

	@Override
	public Path locateRarExecutable()
	{
		// 1. try the default strategy
		Path rarExe = super.locateRarExecutable();
		if (rarExe != null)
		{
			return rarExe;
		}
		// 2. try the os-specific strategy
		rarExe = searchRarExecutableInPathEnvironmentVariable();
		if (rarExe != null)
		{
			return rarExe;
		}
		return null;
	}

	@Override
	protected List<Path> getWinRarStandardInstallationDirectories()
	{
		return ImmutableList.of(Paths.get("/usr/bin"));
	}

	private Path searchRarExecutableInPathEnvironmentVariable()
	{
		// for example: PATH=/opt/bin:/usr/bin
		String pathValue = System.getenv("PATH");
		log.debug("Trying to locate RAR executable in PATH environment  variable: {}", pathValue);
		return returnFirstValidRarExecutable(Pattern.compile(":").splitAsStream(pathValue).map((String path) -> Paths.get(path))::iterator);
	}

	private static class UnixWinRarPackager extends WinRarPackager
	{
		private UnixWinRarPackager(Path rarExecutable)
		{
			super(rarExecutable);
		}

		@Override
		protected boolean isRecyclingSupported()
		{
			return false;
		}
	}
}
