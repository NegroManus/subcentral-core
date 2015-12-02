package de.subcentral.support.winrar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.util.IOUtil;
import de.subcentral.core.util.IOUtil.ProcessResult;
import de.subcentral.core.util.StringUtil;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.support.winrar.WinRarPackConfig.OverwriteMode;
import de.subcentral.support.winrar.WinRarPackResult.Flag;

public abstract class WinRarPackager
{
	private static final Logger	log	= LogManager.getLogger(WinRarPackager.class.getName());

	protected final WinRar		winRar;
	protected final Path		rarExecutable;

	WinRarPackager(WinRar winRar, Path rarExecutable)
	{
		this.winRar = Objects.requireNonNull(winRar, "winRar");
		// do not validate because it can be a non-absolute path
		this.rarExecutable = Objects.requireNonNull(rarExecutable, "rarExecutable");
		log.debug("Using RAR executable at {}", this.rarExecutable);
	}

	public Path getRarExecutable()
	{
		return rarExecutable;
	}

	/**
	 * "Available in Windows version only."
	 * 
	 * @return
	 */
	protected abstract boolean isRecyclingSupported();

	public boolean validate(Path archive)
	{
		try
		{
			return IOUtil.executeProcess(buildValidateCommand(archive), 1, TimeUnit.MINUTES, winRar.getProcessExecutor()).getExitValue() == 0;
		}
		catch (IOException | InterruptedException | TimeoutException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	protected List<String> buildValidateCommand(Path archive)
	{
		/**
		 * Using command t
		 * 
		 * <pre>
		 *     t       Test archive files. This command performs a dummy file
		 *             extraction, writing nothing to the output stream, in order to
		 *             validate the specified file(s).
		 * </pre>
		 */
		// POSIX expects a command list which contains:
		// 1) the executable as first element
		// 2)-n) each argument as an element
		return ImmutableList.of(rarExecutable.toString(), "t", archive.toString());
	}

	public void unpack(Path archive, Path targetDir) throws IOException, InterruptedException, TimeoutException
	{
		IOUtil.executeProcess(buildUnpackCommand(archive, targetDir), 10, TimeUnit.SECONDS, winRar.getProcessExecutor());
	}

	protected List<String> buildUnpackCommand(Path archive, Path targetDir)
	{
		/**
		 * <pre>
		 *  e       Extract files without archived paths.
		 * 
		 *             Extract files excluding their path component, so all files
		 *             are created in the same destination directory.
		 * </pre>
		 */
		// POSIX expects a command list which contains:
		// 1) the executable as first element
		// 2)-n) each argument as an element
		return ImmutableList.of(rarExecutable.toString(), "e", "-o+", archive.toString(), "*", StringUtil.quoteString(targetDir.toString()));
	}

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
			ProcessResult result = IOUtil.executeProcess(buildPackCommand(source, target, cfg), cfg.getTimeoutValue(), cfg.getTimeoutUnit(), winRar.getProcessExecutor());
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

	protected List<String> buildPackCommand(Path source, Path target, WinRarPackConfig cfg)
	{
		List<String> args = new ArrayList<>(8);

		// WinRAR command
		args.add("a"); // A - add to an archive

		// WinRAR switches
		args.add("-ep"); // -EP - exclude paths from names
		args.add("-m" + cfg.getCompressionMethod().getCode()); // -M<n> - set compression method
		args.add("-y"); // -Y - assume Yes on all queries
		switch (cfg.getTargetOverwriteMode())
		{
			case SKIP:
				// "-o- Skip existing files."
				args.add("-o-");
				break;
			case UPDATE:
				// "-o+ Overwrite all
				// (default for updating archived files);"
				args.add("-o+");
				break;
			case REPLACE:
				// do not set the overwrite mode as it does not matter because the target file is deleted anyway
				// in de.subcentral.support.winrar.WinRarPackager.pack(Path, Path, WinRarPackConfig) if it existed
				break;
		}
		switch (cfg.getSourceDeletionMode())
		{
			case KEEP:
				// don't add a delete switch
				break;
			case RECYCLE:
				if (isRecyclingSupported())
				{
					log.warn("Configuration value sourceDelectionMode={} is ignored. This option is not available on this operating system. Files are kept.", DeletionMode.RECYCLE);
				}
				else
				{
					// "-dr Delete files to Recycle Bin
					// Delete files after archiving and place them to Recycle Bin.
					// Available in Windows version only."
					args.add("-dr");
				}
				break;
			case DELETE:
				// -DF - delete files after archiving
				args.add("-df");
				break;
		}

		// target package
		args.add(target.toString());

		// source file
		args.add(source.toString());

		// POSIX expects a command list which contains:
		// 1) the executable as first element
		// 2)-n) each argument as an element
		List<String> command = new ArrayList<>(1 + args.size());
		command.add(rarExecutable.toString());
		command.addAll(args);
		return command;
	}
}
