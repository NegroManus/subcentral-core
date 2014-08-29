package de.subcentral.support.winrar;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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

	protected static final String	RESOURCE_FOLDER	= "de/subcentral/core/io/winrar/";
	protected final Path			rarExe;

	WinRarPackager(RarExeLocation rarExeLocationSpecifier, Path rarExe)
	{
		switch (rarExeLocationSpecifier)
		{
			case EXPLICIT:
				this.rarExe = rarExe;
				break;
			case AUTO_LOCATE:
				this.rarExe = locateRarExecutable();
				break;
			case RESOURCE:
				try
				{
					this.rarExe = loadRarResource();
					break;
				}
				catch (URISyntaxException e)
				{
					throw new IllegalStateException(e);
				}
			default:
				throw new IllegalArgumentException("Invalid RarExeLocation:" + rarExeLocationSpecifier);
		}
	}

	protected abstract Path loadRarResource() throws URISyntaxException;

	protected static final Path loadRarResource(String filename) throws URISyntaxException
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
