package de.subcentral.core.io.winrar;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.io.IOUtil;
import de.subcentral.core.io.winrar.WinRar.RarExeLocation;

class WindowsWinRarPackager extends WinRarPackager
{
	private static final Logger				log								= LoggerFactory.getLogger(WindowsWinRarPackager.class);

	/**
	 * The Console Rar can only pack RAR archives, but it does not open a GUI.
	 */
	private static final String				RAR_EXE_FILENAME				= "Rar.exe";

	/**
	 * The typical WinRAR installation directories on Windows.
	 */
	private static final ImmutableSet<Path>	WELL_KNOWN_WINRAR_DIRECTORIES	= ImmutableSet.of(Paths.get("C:\\Program Files\\WinRAR"),
																					Paths.get("C:\\Program Files (x86)\\WinRAR"));

	private static final String				RESOURCE_RAR_EXE_32				= "rar_5.10_win_x32.exe";
	private static final String				RESOURCE_RAR_EXE_64				= "rar_5.10_win_x64.exe";

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
	private static Path findWinRarInstallDir()
	{
		// 1. try the well known WinRAR directories
		Path winRarInstallation = returnFirstValid(WELL_KNOWN_WINRAR_DIRECTORIES);
		if (winRarInstallation != null)
		{
			log.info("Found WinRAR directory: {}", winRarInstallation);
			return winRarInstallation;
		}

		// 2. If that fails, query the registry
		winRarInstallation = queryRegistryForWinRarInstallation();
		if (winRarInstallation != null)
		{
			log.info("Found WinRAR directory: {}", winRarInstallation);
			return winRarInstallation;
		}

		log.warn("Could not find WinRAR directory. Returning null");
		return null;
	}

	private static Path queryRegistryForWinRarInstallation()
	{
		Path winRarInstallation = null;

		String[] command = new String[] { "REG", "QUERY", "\"HKEY_LOCAL_MACHINE\\Software\\WinRARs\"" };
		ProcessBuilder builder = new ProcessBuilder(command);
		log.debug("Querying Windows registry for WinRAR directory: {}", command, "");
		try
		{
			Process p = builder.start();
			try (InputStream es = p.getErrorStream();)
			{
				String errorMsg = IOUtil.readInputStream(es);
				if (!errorMsg.isEmpty())
				{
					log.error("Could not find WinRAR directory: Command {} returned error message. Returning null\n\"{}\"", command, errorMsg);
					return null;
				}
			}
			try (InputStream is = p.getInputStream(); Scanner scanner = new Scanner(is);)
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
						winRarInstallation = Paths.get(exePath).getParent();
						break;
					}
				}
				log.debug("Full output of command:\n\"{}\"", Joiner.on('\n').join(outputLines));

				if (winRarInstallation == null)
				{
					log.warn("Could not find WinRAR directory in the Windows registry. Returning null");
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
			validateWinRarInstallation(winRarInstallation);
		}
		catch (Exception e)
		{
			log.warn("WinRAR directory {} from registry seems not be valid ({}). Will use it anyway but errors may occur while packaging.",
					winRarInstallation,
					e);
		}
		return winRarInstallation;
	}

	private static Path returnFirstValid(Set<Path> possibleWinRarDirectories)
	{
		log.debug("Checking if valid WinRAR directory: {}", possibleWinRarDirectories);
		for (Path path : possibleWinRarDirectories)
		{
			try
			{
				validateWinRarInstallation(path);
				log.info("Returning valid WinRAR directory: {}", path);
				return path;
			}
			catch (Exception e)
			{
				log.debug("{} was not a valid WinRAR directory ({})", path, e.toString());
			}
		}
		log.info("Found no valid WinRAR directory (none of {} was valid)", possibleWinRarDirectories);
		return null;
	}

	private static Path validateWinRarInstallation(Path winRarDirectory) throws NullPointerException, NotDirectoryException, SecurityException,
			NoSuchFileException
	{
		if (winRarDirectory == null)
		{
			throw new NullPointerException("WinRAR directory cannot be null");
		}

		if (!Files.isDirectory(winRarDirectory))
		{
			throw new NotDirectoryException(winRarDirectory.toString());
		}

		Path rarExecutable = winRarDirectory.resolve(RAR_EXE_FILENAME);
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

	WindowsWinRarPackager(RarExeLocation rarExeLocationSpecifier, Path rarExe)
	{
		super(rarExeLocationSpecifier, rarExe);
	}

	@Override
	protected Path loadRarResource() throws URISyntaxException
	{
		if (SystemUtils.OS_ARCH.contains("64"))
		{
			return loadRarResource(RESOURCE_RAR_EXE_64);
		}
		else
		{
			return loadRarResource(RESOURCE_RAR_EXE_32);
		}
	}

	@Override
	protected Path locateRarExecutable()
	{
		return findWinRarInstallDir().resolve(RAR_EXE_FILENAME);
	}

	@Override
	protected List<String> buildCommand(Path source, Path target, WinRarPackConfig cfg)
	{
		List<String> args = new ArrayList<>(9);

		// WinRAR command
		args.add("a"); // A - add to an archive

		// WinRAR switches
		args.add("-ep"); // -EP - exclude paths from names
		args.add("-m" + cfg.getCompressionMethod().getCode()); // -M<n> - set compression method
		args.add("-y"); // -Y - assume Yes on all queries
		if (cfg.getReplaceTarget()) // -O[+|-] - set the overwrite mode
		{
			args.add("-o+");
		}
		else
		{
			args.add("-o-");
		}
		if (cfg.getDeleteSource())
		{
			args.add("-df"); // -DF - delete files after archiving
		}

		// target package
		args.add(target.toString());

		// sourceFile
		args.add(source.toString());

		// COMMAND
		List<String> command = new ArrayList<>();
		command.add(rarExe.toString());
		command.addAll(args);
		return command;
	}

}
