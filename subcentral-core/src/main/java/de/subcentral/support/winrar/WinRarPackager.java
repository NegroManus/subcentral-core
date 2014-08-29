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
import java.util.Locale;

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

	WinRarPackager(RarExeLocation rarExeLocationSpecifier, Path rarExecutable)
	{
		try
		{
			switch (rarExeLocationSpecifier)
			{
				case SPECIFY:
					this.rarExecutable = validateRarExecutable(rarExecutable);
					break;
				case LOCATE:
					this.rarExecutable = locateRarExecutable();
					break;
				case RESOURCE:
					this.rarExecutable = loadRarExecutableAsResource();
					break;

				default:
					throw new IllegalArgumentException("Invalid RarExeLocation:" + rarExeLocationSpecifier);
			}
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Exception while initializing rar executable", e);
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

	public WinRarPackResult pack(Path source, Path target, WinRarPackConfig cfg) throws IOException, InterruptedException
	{
		try
		{
			EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
			boolean targetExists = Files.exists(target, LinkOption.NOFOLLOW_LINKS);
			if (targetExists)
			{
				flags.add(Flag.ALREADY_EXISTED);
			}

			ProcessBuilder processBuilder = new ProcessBuilder(buildCommand(source, target, cfg));
			log.debug("Executing {}", processBuilder.command());
			Process process = processBuilder.start();
			try (InputStream errStream = process.getErrorStream())
			{
				String errMsg = IOUtil.readInputStream(errStream);
				if (!errMsg.isEmpty())
				{
					throw new IOException("Executing command " + processBuilder.command() + " resulted in error message: " + errMsg);
				}
			}
			process.getInputStream().close();
			process.getOutputStream().close();
			int exitCode = process.waitFor();
			if (exitCode != WinRarPackResult.EXIT_CODE_SUCCESSFUL && exitCode != WinRarPackResult.EXIT_CODE_NO_FILES_MATCHING_MASK_AND_OPTIONS)
			{
				throw new IOException("Executing command \"" + processBuilder.command() + "\" returned code: " + exitCode + " ("
						+ WinRarPackResult.getExitCodeDescription(exitCode, Locale.ENGLISH) + ")");
			}

			if (targetExists && cfg.getReplaceTarget())
			{
				flags.add(Flag.REPLACED);
			}
			if (cfg.getDeleteSource() && Files.notExists(source))
			{
				flags.add(Flag.DELETED_SOURCE);
			}
			return new WinRarPackResult(flags);
		}
		catch (Exception e)
		{
			return new WinRarPackResult(e);
		}
	}

	protected abstract List<String> buildCommand(Path source, Path target, WinRarPackConfig cfg);
}
