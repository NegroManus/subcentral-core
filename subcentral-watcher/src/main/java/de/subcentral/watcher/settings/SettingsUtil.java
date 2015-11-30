package de.subcentral.watcher.settings;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

public class SettingsUtil
{
	private static final Logger log = LogManager.getLogger(SettingsUtil.class);

	public static <V, T extends DeactivatableSettingEntry<V>> ImmutableList<V> getValuesOfEnabledSettingEntries(Iterable<T> entries)
	{
		ImmutableList.Builder<V> enabledEntries = ImmutableList.builder();
		for (T entry : entries)
		{
			if (entry.isEnabled())
			{
				enabledEntries.add(entry.getValue());
			}
		}
		return enabledEntries.build();
	}

	public static Path getLocalConfigDirectorySave()
	{
		Path localConfigDir;
		try
		{
			localConfigDir = SettingsUtil.getLocalConfigDirectory();
		}
		catch (NoSuchFileException | UnsupportedOperationException e)
		{
			localConfigDir = Paths.get(SystemUtils.USER_DIR);
			log.warn("Could not find local configuration directory. Using user's current working directory to save settings: " + localConfigDir, e);
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

	private static String getUnixConfigHome()
	{
		String configHome = System.getenv("XDG_CONFIG_HOME");
		if (configHome == null || configHome.trim().length() == 0)
		{
			configHome = System.getenv("HOME") + SystemUtils.FILE_SEPARATOR + ".config";
		}
		return configHome;
	}

	private static String getWindowsAppData()
	{
		return System.getenv("AppData");
	}

	public static void main(String[] args) throws NoSuchFileException, UnsupportedOperationException
	{
		System.out.println(getLocalConfigDirectory());
	}

	public SettingsUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
