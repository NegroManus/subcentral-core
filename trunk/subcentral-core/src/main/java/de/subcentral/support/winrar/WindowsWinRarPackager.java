package de.subcentral.support.winrar;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.util.IOUtil;
import de.subcentral.support.winrar.WinRar.LocateStrategy;

class WindowsWinRarPackager extends WinRarPackager
{
	private static final Logger	log						= LogManager.getLogger(WindowsWinRarPackager.class.getName());

	/**
	 * The Console RAR can only pack RAR archives, but it does not open a GUI.
	 */
	private static final String	RAR_EXECUTABLE_FILENAME	= "RAR.exe";

	public static Path tryLocateRarExecutable()
	{
		// 1. try the well known WinRAR directories
		Path rarExecutable = searchRarExecutableInWellKnownDirectories();
		if (rarExecutable != null)
		{
			log.info("Found valid RAR executable: {}", rarExecutable);
			return rarExecutable;
		}

		// 2. if that fails, query the registry
		rarExecutable = queryWindowsRegistryForRarExecutable();
		if (rarExecutable != null)
		{
			log.info("Found valid RAR executable: {}", rarExecutable);
			return rarExecutable;
		}

		log.warn("Could not find valid RAR executable. Returning null");
		return null;
	}

	public static Path searchRarExecutableInWellKnownDirectories()
	{
		// The typical WinRAR installation directories on Windows.
		return returnFirstValidRarExecutable(ImmutableSet.of(Paths.get("C:\\Program Files\\WinRAR"), Paths.get("C:\\Program Files (x86)\\WinRAR")));
	}

	/**
	 * All entries under a registry node:
	 * 
	 * <pre>
	 * C:\Users\Max>reg query "HKEY_LOCAL_MACHINE\Software\WinRAR"
	 * 
	 * HKEY_LOCAL_MACHINE\Software\WinRAR
	 *     exe64    REG_SZ    C:\Program Files\WinRAR\WinRAR.exe
	 * 
	 * HKEY_LOCAL_MACHINE\Software\WinRAR\Capabilities
	 * </pre>
	 * 
	 * A specific entry under a registry node with the "/v <name>" option:
	 * 
	 * <pre>
	 * C:\Users\Max>reg query "HKEY_LOCAL_MACHINE\Software\WinRAR" /v exe64
	 * 
	 * HKEY_LOCAL_MACHINE\Software\WinRAR
	 *     exe64    REG_SZ    C:\Program Files\WinRAR\WinRAR.exe
	 * </pre>
	 * 
	 * @return
	 */
	public static Path queryWindowsRegistryForRarExecutable()
	{
		Path rarExecutable = null;

		String[] command = new String[] { "REG", "QUERY", "\"HKEY_LOCAL_MACHINE\\Software\\WinRAR\"" };
		ProcessBuilder builder = new ProcessBuilder(command);
		log.debug("Querying Windows registry for WinRAR installation directory: {}", builder.command(), "");
		try
		{
			Process p = builder.start();
			try (InputStream es = p.getErrorStream();)
			{
				String errorMsg = IOUtil.readInputStream(es);
				if (!errorMsg.isEmpty())
				{
					log.error("Could not locate WinRAR installation directory in Windows registry: Command {} returned error message. Returning null\n\"{}\"",
							builder.command(),
							errorMsg);
					return null;
				}
			}
			try (InputStream is = p.getInputStream(); Scanner scanner = new Scanner(is, Charset.defaultCharset().name());)
			{
				List<String> outputLines = new ArrayList<>();
				Pattern pExeEntry = Pattern.compile("\\s*(exe(\\d+)?)\\s+REG_SZ\\s+(.*)", Pattern.CASE_INSENSITIVE);
				Matcher mExeEntry = pExeEntry.matcher("");
				scanner.useDelimiter("\r\n");
				while (scanner.hasNext())
				{
					String line = scanner.next();
					outputLines.add(line);
					if (mExeEntry.reset(line).matches())
					{
						String exe = mExeEntry.group(1);
						String exePath = mExeEntry.group(3);
						log.debug("Found \"exe*\" entry in registry: \"{}\" -> \"{}\"", exe, exePath);
						rarExecutable = Paths.get(exePath);
						break;
					}
				}
				log.debug("Full output of command:\n\"{}\"", Joiner.on('\n').join(outputLines));

				if (rarExecutable == null)
				{
					log.warn("Could not locate WinRAR installation directory in Windows registry. Returning null");
					return null;
				}
			}
		}
		catch (IOException e)
		{
			log.error("Exception while querying Windows registry for WinRAR installation directory. Returning null", e);
			return null;
		}

		try
		{
			validateRarExecutable(rarExecutable);
		}
		catch (Exception e)
		{
			log.error("RAR executable in WinRAR installation directory ({}) is invalid: {}", rarExecutable, e);
			return null;
		}
		return rarExecutable;
	}

	private static Path returnFirstValidRarExecutable(Set<Path> possibleWinRarDirectories)
	{
		log.debug("Trying to locate RAR executable in directories: {}", possibleWinRarDirectories);
		for (Path path : possibleWinRarDirectories)
		{
			Path candidate = path.resolve(RAR_EXECUTABLE_FILENAME);
			try
			{
				validateRarExecutable(candidate);
				log.debug("Found valid RAR executable: {}", candidate);
				return candidate;
			}
			catch (Exception e)
			{
				log.debug("{} was no valid RAR executable: {}", candidate, e.toString());
			}
		}
		log.debug("Could not locate RAR executable in directories {}", possibleWinRarDirectories);
		return null;
	}

	WindowsWinRarPackager(LocateStrategy locateStrategy, Path rarExe)
	{
		super(locateStrategy, rarExe);
	}

	@Override
	protected String determineRarExecutableResourceFilename()
	{
		if (SystemUtils.OS_ARCH.contains("64"))
		{
			return "rar_5.10_win_x32.exe";
		}
		else
		{
			return "rar_5.10_win_x64.exe";
		}
	}

	@Override
	protected Path locateRarExecutable()
	{
		return tryLocateRarExecutable();
	}

	@Override
	protected List<String> buildCommand(Path source, Path target, WinRarPackConfig cfg)
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
				// "-dr Delete files to Recycle Bin
				// Delete files after archiving and place them to Recycle Bin.
				// Available in Windows version only."
				args.add("-dr");
				break;
			case DELETE:
				// "-DF - delete files after archiving"
				args.add("-df");
				break;
		}

		// target package
		args.add(target.toString());

		// source file
		args.add(source.toString());

		// Windows expects a command list which contains:
		// 1) the executable as first element
		// 2)-n) each argument as an element
		List<String> command = new ArrayList<>(1 + args.size());
		command.add(rarExecutable.toString());
		command.addAll(args);
		return command;
	}

}
