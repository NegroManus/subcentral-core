package de.subcentral.support.winrar;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;

import de.subcentral.support.winrar.WinRar.LocateStrategy;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;

class UnixWinRarPackager extends WinRarPackager
{
	private static final Logger	log				= LogManager.getLogger(UnixWinRarPackager.class.getName());

	private static final String	RAR_EXECUTABLE	= "rar";

	UnixWinRarPackager(LocateStrategy locateStrategy, Path rarExe)
	{
		super(locateStrategy, rarExe);
	}

	@Override
	protected String determineRarExecutableResourceFilename()
	{
		if (SystemUtils.IS_OS_LINUX)
		{
			if (SystemUtils.OS_ARCH.contains("64"))
			{
				return "rar_5.10_linux_x64";
			}
			else
			{
				return "rar_5.10_linux_x32";
			}
		}
		else if (SystemUtils.IS_OS_MAC_OSX)
		{
			return "rar_5.10_macosx";
		}
		else if (SystemUtils.IS_OS_FREE_BSD)
		{
			return "rar_5.10_freebsd";
		}
		// default
		return "rar_5.10_linux_x32";
	}

	@Override
	protected Path locateRarExecutable()
	{
		return Paths.get(RAR_EXECUTABLE);
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
				// "Available in Windows version only."
				log.warn("configuration item sourceDelectionMode={} is ignored. This option is only available in Windows versions of WinRAR",
						DeletionMode.RECYCLE);
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

		// Unix expects a command list which contains exactly two elements:
		// 1) the executable
		// 2) the argument(s), separated with whitespace
		List<String> command = new ArrayList<>(2);
		command.add(RAR_EXECUTABLE);
		command.add(Joiner.on(' ').join(args));
		return command;
	}
}
