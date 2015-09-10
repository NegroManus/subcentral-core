package de.subcentral.support.winrar;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.util.IOUtil;
import de.subcentral.core.util.IOUtil.ProcessResult;

public class WindowsWinRar extends WinRar
{
	private static final Logger log = LogManager.getLogger(WindowsWinRar.class.getName());

	/**
	 * The console program RAR.exe can only pack RAR archives, but it does not open a GUI. WinRAR.exe can pack ZIP archives, but it opens a GUI - so do not use that.
	 */
	private static final Path RAR_EXECUTABLE_FILENAME = Paths.get("Rar.exe");

	@Override
	public Path getRarExecutableFilename()
	{
		return RAR_EXECUTABLE_FILENAME;
	}

	@Override
	public Path locateRarExecutable() throws IllegalStateException
	{
		// 1. try the default strategy
		Path rarExe = super.locateRarExecutable();
		if (rarExe != null)
		{
			return rarExe;
		}
		// 2. try the os-specific strategy
		rarExe = queryWindowsRegistryForRarExe();
		if (rarExe != null)
		{
			return rarExe;
		}
		return null;
	}

	@Override
	protected List<Path> getWinRarStandardInstallationDirectories()
	{
		return ImmutableList.of(Paths.get("C:\\Program Files\\WinRAR"), Paths.get("C:\\Program Files (x86)\\WinRAR"));
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
	private Path queryWindowsRegistryForRarExe()
	{
		List<String> command = ImmutableList.of("reg", "query", "HKEY_LOCAL_MACHINE\\Software\\WinRAR");
		try
		{
			ProcessResult result = IOUtil.executeProcess(command, 1, TimeUnit.MINUTES);
			if (result.getExitValue() != 0 || result.getStdErr() != null)
			{
				log.warn("Could not locate WinRAR installation directory using Windows registry: Command {} exited with value {} and standard error output was \"{}\". Returning null",
						command,
						result.getExitValue(),
						result.getStdErr());
				return null;
			}
			Pattern pExeEntry = Pattern.compile("^\\s*exe(?:\\d+)?\\s+REG_SZ\\s+(.*?)\\s*$", Pattern.MULTILINE);
			Matcher mExeEntry = pExeEntry.matcher(result.getStdOut());
			if (mExeEntry.find())
			{
				String exePath = mExeEntry.group(1);
				Path winRarExe = Paths.get(exePath);
				Path rarExe = winRarExe.resolveSibling(RAR_EXECUTABLE_FILENAME);
				try
				{
					validateRarExecutable(rarExe);
					log.debug("Found RAR executable using Windows registry at {}", rarExe);
					return rarExe;
				}
				catch (Exception e)
				{
					log.warn("Could not locate WinRAR installation directory using Windows registry: " + rarExe + " is not a valid rar executable", e);
					return null;
				}
			}
			else
			{
				log.warn("Could not locate WinRAR installation directory using Windows registry: Output of command {} could not be parsed. Output was: \"{}\"", command, result.getStdOut());
				return null;
			}
		}
		catch (Exception e)
		{
			log.warn("Could not locate WinRAR installation directory using Windows registry: Execution of command " + command + " failed", e);
			return null;
		}
	}

	@Override
	public WinRarPackager getPackager(Path rarExecutable)
	{
		return new WindowsWinRarPackager(rarExecutable);
	}

	private static class WindowsWinRarPackager extends WinRarPackager
	{
		private WindowsWinRarPackager(Path rarExecutable)
		{
			super(rarExecutable);
		}

		@Override
		protected boolean isRecyclingSupported()
		{
			return true;
		}
	}
}
