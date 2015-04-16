package de.subcentral.support.winrar;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.IOUtil;
import de.subcentral.core.util.IOUtil.CommandResult;
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
					this.rarExecutable = validateExecutable(rarExecutable);
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

	protected static final Path validateExecutable(Path exe) throws NullPointerException, NoSuchFileException, SecurityException
	{
		if (exe == null)
		{
			throw new NullPointerException("Executable cannot be null");
		}
		if (!Files.isRegularFile(exe, LinkOption.NOFOLLOW_LINKS))
		{
			throw new NoSuchFileException(exe.toString());
		}
		if (!Files.isExecutable(exe))
		{
			throw new SecurityException("Executable is not executable: " + exe);
		}
		return exe;
	}

	protected abstract String determineRarExecutableResourceFilename();

	protected abstract Path locateRarExecutable();

	public boolean validate(Path archive)
	{
		try
		{
			return IOUtil.executeCommand(buildValidateCommand(archive), 1, TimeUnit.MINUTES).getExitValue() == 0;
		}
		catch (IOException | InterruptedException | TimeoutException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	protected abstract List<String> buildValidateCommand(Path archive);

	public void unpack(Path archive, Path targetDir) throws IOException, InterruptedException, TimeoutException
	{
		CommandResult result = IOUtil.executeCommand(buildUnpackCommand(archive, targetDir), 10, TimeUnit.SECONDS);
	}

	protected abstract List<String> buildUnpackCommand(Path archive, Path targetDir);

	/**
	 * Packs a single file into a single WinRAR archive.
	 * 
	 * @param source
	 *            the source file to pack, not null
	 * @param target
	 *            the target archive, not null (may or not exist yet)
	 * @param cfg
	 *            the packaging configuration, not null
	 * @return the result of the packaging operation
	 */
	public WinRarPackResult pack(Path source, Path target, WinRarPackConfig cfg)
	{
		Objects.requireNonNull(source, "source");
		Objects.requireNonNull(target, "target");
		Objects.requireNonNull(cfg, "cfg");

		EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
		int exitValue = -1;
		String logMsg = null;
		String errMsg = null;
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
				}
			}
			CommandResult result = IOUtil.executeCommand(buildPackCommand(source, target, cfg), cfg.getTimeoutValue(), cfg.getTimeoutUnit());
			exitValue = result.getExitValue();
			logMsg = result.getStdOut();
			errMsg = result.getStdErr();

			// may add tags
			if (targetExisted && Files.getLastModifiedTime(target, LinkOption.NOFOLLOW_LINKS).toMillis() > startTime)
			{
				switch (cfg.getTargetOverwriteMode())
				{
					case UPDATE:
						flags.add(Flag.TARGET_UPDATED);
						break;
					case REPLACE:
						flags.add(Flag.TARGET_REPLACED);
						break;
					default:
						break;
				}
			}
			// if DeletionMode.DELETE || DeletionMode.RECYCLE and it really was deleted
			if (cfg.getSourceDeletionMode() != DeletionMode.KEEP && Files.notExists(source))
			{
				flags.add(Flag.SOURCE_DELETED);
			}

			// return result
			if (result.getStdErr() == null)
			{
				return new WinRarPackResult(exitValue, flags, logMsg);
			}
			else
			{
				return new WinRarPackResult(exitValue, flags, new IOException(errMsg), logMsg);
			}
		}
		catch (IOException | InterruptedException | RuntimeException | TimeoutException e)
		{
			log.warn("Exception while packing", e);
			return new WinRarPackResult(exitValue, flags, e, logMsg);
		}
	}

	protected abstract List<String> buildPackCommand(Path source, Path target, WinRarPackConfig cfg);

	private static final Path loadResource(String filename) throws URISyntaxException
	{
		return Paths.get(WinRarPackager.class.getClassLoader().getResource("de/subcentral/support/winrar/" + filename).toURI());
	}
}
