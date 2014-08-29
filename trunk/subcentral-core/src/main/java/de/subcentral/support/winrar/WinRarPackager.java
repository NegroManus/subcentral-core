package de.subcentral.support.winrar;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.subcentral.core.util.IOUtil;
import de.subcentral.support.winrar.WinRar.RarExeLocation;
import de.subcentral.support.winrar.WinRarPackResult.Flag;

public abstract class WinRarPackager
{
	private static final Logger		log				= LoggerFactory.getLogger(WinRarPackager.class);

	protected static final String	RESOURCE_FOLDER	= "de/subcentral/support/winrar/";
	protected final Path			rarExecutable;

	WinRarPackager(RarExeLocation rarExeLocation, Path rarExecutable)
	{
		try
		{
			switch (rarExeLocation)
			{
				case SPECIFY:
					this.rarExecutable = validateRarExecutable(rarExecutable);
					break;
				case LOCATE:
					this.rarExecutable = locateRarExecutable();
					if (this.rarExecutable == null)
					{
						throw new IllegalStateException("Could not locate Rar executable");
					}
					break;
				case RESOURCE:
					this.rarExecutable = loadRarExecutableAsResource();
					break;
				default:
					throw new IllegalArgumentException("Invalid RarExeLocation value:" + rarExeLocation);
			}
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Exception while initializing rar executable: " + e, e);
		}
	}

	public Path getRarExecutable()
	{
		return rarExecutable;
	}

	protected static final Path validateRarExecutable(Path rarExecutable) throws NullPointerException, NoSuchFileException, SecurityException
	{
		if (rarExecutable == null)
		{
			throw new NullPointerException("Rar executable cannot be null");
		}
		if (!Files.isRegularFile(rarExecutable, LinkOption.NOFOLLOW_LINKS))
		{
			throw new NoSuchFileException(rarExecutable.toString());
		}
		if (!Files.isExecutable(rarExecutable))
		{
			throw new SecurityException("Not executable: " + rarExecutable);
		}
		return rarExecutable;
	}

	protected abstract Path loadRarExecutableAsResource() throws Exception;

	protected static final Path loadResource(String filename) throws URISyntaxException
	{
		return Paths.get(WinRarPackager.class.getClassLoader().getResource(RESOURCE_FOLDER + '/' + filename).toURI());
	}

	protected abstract Path locateRarExecutable();

	public WinRarPackResult pack(Path source, Path target, WinRarPackConfig cfg)
	{
		int exitCode = -1;
		EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
		try
		{
			long startTime = System.currentTimeMillis();
			boolean targetExists = Files.exists(target, LinkOption.NOFOLLOW_LINKS);
			if (targetExists)
			{
				flags.add(Flag.ALREADY_EXISTED);
			}

			ProcessBuilder processBuilder = new ProcessBuilder(buildCommand(source, target, cfg));
			log.debug("Executing {}", processBuilder.command());
			Process process = processBuilder.start();
			String errMsg = null;
			try (InputStream errStream = process.getErrorStream())
			{
				errMsg = StringUtils.trim(IOUtil.readInputStream(errStream));
			}
			process.getInputStream().close();
			process.getOutputStream().close();
			boolean exitedBeforeTimeout = process.waitFor(cfg.getTimeout(), cfg.getTimeoutUnit());
			exitCode = process.exitValue();

			// may add tags
			if (targetExists && cfg.getReplaceTarget() && Files.getLastModifiedTime(target, LinkOption.NOFOLLOW_LINKS).toMillis() > startTime)
			{
				flags.add(Flag.REPLACED);
			}
			if (cfg.getDeleteSource() && Files.notExists(source))
			{
				flags.add(Flag.DELETED_SOURCE);
			}

			// return result
			if (!exitedBeforeTimeout)
			{
				return new WinRarPackResult(exitCode, flags, new TimeoutException("Rar process timed out after " + cfg.getTimeout() + " "
						+ cfg.getTimeoutUnit()));
			}
			if (StringUtils.isBlank(errMsg))
			{
				return new WinRarPackResult(exitCode, flags);
			}
			else
			{
				return new WinRarPackResult(exitCode, flags, new IOException(errMsg));
			}
		}
		catch (Exception e)
		{
			return new WinRarPackResult(exitCode, flags, e);
		}
	}

	protected abstract List<String> buildCommand(Path source, Path target, WinRarPackConfig cfg);
}
