package de.subcentral.support.winrar;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;

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
		// 1. try the well known WinRAR directories
		Path rarExecutable = searchRarExecutableInWellKnownDirectories();
		if (rarExecutable != null)
		{
			return rarExecutable;
		}
		// 2. if that fails, search in PATH environment variable
		rarExecutable = searchRarExecutableInPathEnvironmentVariable();
		if (rarExecutable != null)
		{
			return rarExecutable;
		}
		return null;
	}

	private Path searchRarExecutableInWellKnownDirectories()
	{
		// The typical WinRAR installation directories on Unix
		Set<Path> wellKnownDirs = ImmutableSet.of(Paths.get("/usr/bin"));
		log.debug("Trying to locate RAR executable in well known directories: {}", wellKnownDirs);
		return returnFirstValidRarExecutable(wellKnownDirs);
	}

	private Path searchRarExecutableInPathEnvironmentVariable()
	{
		// for example: PATH=/opt/bin:/usr/bin
		String pathValue = System.getenv("PATH");
		log.debug("Trying to locate RAR executable in PATH environment  variable: {}", pathValue);
		return returnFirstValidRarExecutable(Arrays.stream(pathValue.split(":")).map((String path) -> Paths.get(path)).collect(Collectors.toSet()));
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
