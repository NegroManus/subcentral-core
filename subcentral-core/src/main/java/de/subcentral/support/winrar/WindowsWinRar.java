package de.subcentral.support.winrar;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.util.IOUtil;
import de.subcentral.support.winrar.WinRarPackager.LocateStrategy;

public class WindowsWinRar extends WinRar
{
    private static final Logger log = LogManager.getLogger(WindowsWinRar.class.getName());

    /**
     * The console program RAR.exe can only pack RAR archives, but it does not open a GUI. WinRAR.exe can pack ZIP archives, but it opens a GUI - so
     * do not use that.
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
	throw new IllegalStateException("Could not locate RAR executable");
    }

    private Path searchRarExecutableInWellKnownDirectories()
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
    private Path queryWindowsRegistryForRarExecutable()
    {
	Path winRarExecutable = null;
	Path rarExecutable = null;

	String[] command = new String[] { "REG", "QUERY", "\"HKEY_LOCAL_MACHINE\\Software\\WinRAR\"" };
	ProcessBuilder builder = new ProcessBuilder(command);
	log.debug("Querying Windows registry for WinRAR installation directory: {}", builder.command());
	try
	{
	    Process p = builder.start();
	    try (InputStream es = p.getErrorStream();)
	    {
		String errorMsg = StringUtils.stripToNull(IOUtil.drainToString(es));
		if (errorMsg != null)
		{
		    log.error("Could not locate WinRAR installation directory in Windows registry: Command {} returned error message: \"{}\". Returning null", builder.command(), errorMsg);
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
			winRarExecutable = Paths.get(exePath);
			rarExecutable = winRarExecutable.resolveSibling(WindowsWinRar.RAR_EXECUTABLE_FILENAME);
			break;
		    }
		}
		log.debug("Full output of command:\n\"{}\"", Joiner.on('\n').join(outputLines));

		if (rarExecutable == null)
		{
		    log.warn("Could not locate WinRAR installation directory in Windows registry: Could not parse output of REG. Returning null");
		    return null;
		}
	    }
	}
	catch (IOException e)
	{
	    log.warn("Exception while querying Windows registry for WinRAR installation directory. Returning null", e);
	    return null;
	}

	try
	{
	    validateRarExecutable(rarExecutable);
	    return rarExecutable;
	}
	catch (Exception e)
	{
	    log.warn("No valid RAR executable in WinRAR installation directory specified in Windows registry ({}): {}. Validating WinRAR executable", rarExecutable, e);
	}

	try
	{
	    validateRarExecutable(winRarExecutable);
	    return winRarExecutable;
	}
	catch (Exception e)
	{
	    log.warn("No valid WinRAR executable in WinRAR installation directory specified in Windows registry ({}): {}. Returning null", winRarExecutable, e);
	}

	return null;
    }

    @Override
    public WinRarPackager getPackager(LocateStrategy locateStrategy, Path rarExecutable)
    {
	return new WindowsWinRarPackager(this, locateStrategy, rarExecutable);
    }
}
