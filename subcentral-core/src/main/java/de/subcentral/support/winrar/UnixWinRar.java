package de.subcentral.support.winrar;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;

import de.subcentral.support.winrar.WinRarPackager.LocateStrategy;

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
    public WinRarPackager getPackager(LocateStrategy locateStrategy, Path rarExecutable)
    {
	return new UnixWinRarPackager(this, locateStrategy, rarExecutable);
    }

    @Override
    public Path locateRarExecutable()
    {
	// 1. try the well known WinRAR directories
	Path rarExecutable = searchRarExecutableInWellKnownDirectories();
	if (rarExecutable != null)
	{
	    log.info("Found valid RAR executable: {}", rarExecutable);
	    return rarExecutable;
	}
	return RAR_EXECUTABLE_FILENAME;
    }

    private Path searchRarExecutableInWellKnownDirectories()
    {
	// The typical WinRAR installation directories on Windows.
	return returnFirstValidRarExecutable(ImmutableSet.of(Paths.get("/usr/bin")));
    }
}
