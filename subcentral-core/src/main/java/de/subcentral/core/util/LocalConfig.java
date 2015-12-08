package de.subcentral.core.util;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;

/**
 * <b>Important:</b> This class cannot use logging because it is used to determine the log directory. So logging can only work after this class is initialized.
 */
public class LocalConfig
{
	public static Path getLocalConfigDirectorySave()
	{
		Path localConfigDir;
		try
		{
			localConfigDir = getLocalConfigDirectory();
		}
		catch (NoSuchFileException | UnsupportedOperationException e)
		{
			localConfigDir = Paths.get(SystemUtils.USER_DIR);
			System.err.println("Could not find local configuration directory. Using user's current working directory to save settings: " + localConfigDir + ". Exception was:");
			e.printStackTrace();
		}
		return localConfigDir;
	}

	public static Path getLocalConfigDirectory() throws UnsupportedOperationException, NoSuchFileException
	{
		Path localConfigDir;
		if (SystemUtils.IS_OS_WINDOWS)
		{
			localConfigDir = Paths.get(getWindowsAppData());
		}
		else if (SystemUtils.IS_OS_UNIX)
		{
			localConfigDir = Paths.get(getUnixConfigHome());
		}
		else
		{
			throw new UnsupportedOperationException("Your operating system is not supported:" + SystemUtils.OS_NAME + " " + SystemUtils.OS_VERSION + " " + SystemUtils.OS_ARCH);
		}
		if (Files.notExists(localConfigDir))
		{
			throw new NoSuchFileException(localConfigDir.toString());
		}
		return localConfigDir;
	}

	public static String getWindowsAppData()
	{
		String appDataDir = System.getenv("AppData");
		if (appDataDir == null || appDataDir.trim().length() == 0)
		{
			throw new UnsupportedOperationException("System environment variable 'AppData' is not set");
		}
		return appDataDir;
	}

	public static String getUnixConfigHome()
	{
		String configHome = System.getenv("XDG_CONFIG_HOME");
		if (configHome == null || configHome.trim().length() == 0)
		{
			configHome = System.getenv("HOME") + SystemUtils.FILE_SEPARATOR + ".config";
		}
		return configHome;
	}

	public LocalConfig()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
