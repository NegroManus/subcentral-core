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
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.IOUtil;
import de.subcentral.support.winrar.WinRar.LocateStrategy;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.support.winrar.WinRarPackConfig.OverwriteMode;
import de.subcentral.support.winrar.WinRarPackResult.Flag;

public abstract class WinRarPackager
{
	private static final Logger	log	= LogManager.getLogger(WinRarPackager.class.getName());
	protected final Path		rarExecutable;

	WinRarPackager(LocateStrategy locateStrategy, Path rarExecutable)
	{
		Objects.requireNonNull(locateStrategy, "locateStrategy");
		try
		{
			switch (locateStrategy)
			{
				case SPECIFY:
					this.rarExecutable = validateRarExecutable(rarExecutable);
					break;
				case LOCATE:
					this.rarExecutable = locateRarExecutable();
					break;
				case RESOURCE:
					this.rarExecutable = loadResource(determineRarExecutableResourceFilename());
					break;
				default:
					throw new IllegalArgumentException("Invalid value for locateStrategy: " + locateStrategy);
			}
			log.info("Using rar executable at {}", this.rarExecutable);
		}
		catch (NoSuchFileException | URISyntaxException | RuntimeException e)
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
			throw new NullPointerException("RAR executable cannot be null");
		}
		if (!Files.isRegularFile(rarExecutable, LinkOption.NOFOLLOW_LINKS))
		{
			throw new NoSuchFileException(rarExecutable.toString());
		}
		if (!Files.isExecutable(rarExecutable))
		{
			throw new SecurityException("RAR executable is not executable: " + rarExecutable);
		}
		return rarExecutable;
	}

	protected abstract String determineRarExecutableResourceFilename();

	protected abstract Path locateRarExecutable();

	/**
	 * Packs a single file into a single WinRAR package.
	 * 
	 * @param source
	 *            the source file to pack, not null
	 * @param target
	 *            the target package, not null (may or not exist yet)
	 * @param cfg
	 *            the packaging configuration, not null
	 * @return the result of the packaging operation
	 */
	public WinRarPackResult pack(Path source, Path target, WinRarPackConfig cfg)
	{
		Objects.requireNonNull(source, "source");
		Objects.requireNonNull(target, "target");
		Objects.requireNonNull(cfg, "cfg");

		int exitCode = -1;
		EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
		String logMsg = null;
		try
		{
			long startTime = System.currentTimeMillis();
			boolean targetExisted = Files.exists(target, LinkOption.NOFOLLOW_LINKS);
			if (targetExisted)
			{
				flags.add(Flag.TARGET_EXISTED);
				if (OverwriteMode.REPLACE == cfg.getTargetOverwriteMode())
				{
					Files.delete(target);
					flags.add(Flag.TARGET_REPLACED);
				}
			}

			ProcessBuilder processBuilder = new ProcessBuilder(buildCommand(source, target, cfg));
			log.debug("Executing {}", processBuilder.command());
			Process process = processBuilder.start();
			process.getOutputStream().close();
			String errMsg = null;
			try (InputStream errStream = process.getErrorStream())
			{
				errMsg = StringUtils.trimToNull(IOUtil.readInputStream(errStream));
			}
			try (InputStream errStream = process.getInputStream())
			{
				logMsg = StringUtils.trimToNull(IOUtil.readInputStream(errStream));
			}
			boolean exitedBeforeTimeout = process.waitFor(cfg.getTimeoutValue(), cfg.getTimeoutUnit());
			exitCode = process.exitValue();
			if (log.isDebugEnabled())
			{
				String timeoutString = exitedBeforeTimeout ? "" : " . Timeout reached: " + cfg.getTimeoutUnit() + " " + cfg.getTimeoutValue();
				log.debug("Execution exited with exit code {} (\"{}\"){}: {} ",
						exitCode,
						WinRarPackResult.getExitCodeDescription(exitCode),
						timeoutString,
						processBuilder.command());
			}

			// may add tags
			if (targetExisted && cfg.getTargetOverwriteMode() == OverwriteMode.UPDATE
					&& Files.getLastModifiedTime(target, LinkOption.NOFOLLOW_LINKS).toMillis() > startTime)
			{
				flags.add(Flag.TARGET_UPDATED);
			}
			if (cfg.getSourceDeletionMode() != DeletionMode.KEEP && Files.notExists(source))
			{
				flags.add(Flag.SOURCE_DELETED);
			}

			// return result
			if (!exitedBeforeTimeout)
			{
				return new WinRarPackResult(exitCode, flags, new TimeoutException("RAR process timed out after " + cfg.getTimeoutValue() + " "
						+ cfg.getTimeoutUnit()), logMsg);
			}
			if (errMsg == null)
			{
				return new WinRarPackResult(exitCode, flags, logMsg);
			}
			else
			{
				return new WinRarPackResult(exitCode, flags, new IOException(errMsg), logMsg);
			}
		}
		catch (IOException | InterruptedException | RuntimeException e)
		{
			log.warn("Exception while packing", e);
			return new WinRarPackResult(exitCode, flags, e, logMsg);
		}
	}

	protected abstract List<String> buildCommand(Path source, Path target, WinRarPackConfig cfg);

	private static final Path loadResource(String filename) throws URISyntaxException
	{
		return Paths.get(WinRarPackager.class.getClassLoader().getResource("de/subcentral/support/winrar/" + filename).toURI());
	}
}
