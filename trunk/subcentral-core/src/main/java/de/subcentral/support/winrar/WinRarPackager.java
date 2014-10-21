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
					if (this.rarExecutable == null)
					{
						throw new IllegalStateException("Could not locate RAR executable");
					}
					break;
				case RESOURCE:
					this.rarExecutable = loadResource(getRarExecutableResourceName());
					break;
				default:
					throw new IllegalArgumentException("Invalid LocateStrategy value:" + locateStrategy);
			}
		}
		catch (NoSuchFileException | NullPointerException | SecurityException | URISyntaxException e)
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
			throw new SecurityException("Not executable: " + rarExecutable);
		}
		return rarExecutable;
	}

	protected abstract String getRarExecutableResourceName();

	protected abstract Path locateRarExecutable();

	/**
	 * Packs a single file into a single WinRAR package.
	 * 
	 * @param source
	 *            the source file to pack
	 * @param target
	 *            the target package (may or not exist yet)
	 * @param cfg
	 *            the packaging configuration
	 * @return the result of the packaging
	 */
	public WinRarPackResult pack(Path source, Path target, WinRarPackConfig cfg)
	{
		log.debug("Packing {} to {} with {}", source, target, cfg);
		int exitCode = -1;
		EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
		try
		{
			if (Files.notExists(source, LinkOption.NOFOLLOW_LINKS))
			{
				// fail-fast behavior
				throw new NoSuchFileException(source.toString());
			}

			long startTime = System.currentTimeMillis();
			boolean targetExists = Files.exists(target, LinkOption.NOFOLLOW_LINKS);
			if (targetExists)
			{
				flags.add(Flag.TARGET_EXISTED);
			}
			if (OverwriteMode.REPLACE == cfg.getTargetOverwriteMode())
			{
				if (Files.deleteIfExists(target))
				{
					flags.add(Flag.TARGET_REPLACED);
				}
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
			boolean exitedBeforeTimeout = process.waitFor(cfg.getTimeoutValue(), cfg.getTimeoutUnit());
			exitCode = process.exitValue();

			// may add tags
			if (targetExists && cfg.getTargetOverwriteMode() == OverwriteMode.UPDATE
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
		catch (IOException | InterruptedException | RuntimeException e)
		{
			return new WinRarPackResult(exitCode, flags, e);
		}
	}

	protected abstract List<String> buildCommand(Path source, Path target, WinRarPackConfig cfg);

	private static final Path loadResource(String filename) throws URISyntaxException
	{
		return Paths.get(WinRarPackager.class.getClassLoader().getResource("de/subcentral/support/winrar/" + filename).toURI());
	}
}
