package de.subcentral.support.winrar;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;

import com.google.common.base.Joiner;

import de.subcentral.support.winrar.WinRar.LocateStrategy;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.support.winrar.WinRarPackConfig.OverwriteMode;

class UnixWinRarPackager extends WinRarPackager
{
	private static final String	RAR_EXECUTABLE	= "rar";

	UnixWinRarPackager(LocateStrategy locateStrategy, Path rarExe)
	{
		super(locateStrategy, rarExe);
	}

	@Override
	protected String getRarExecutableResourceName()
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
		// Cannot respect the archive format. -af not available
		// "This switch is supported only by WinRAR. Console RAR is not able to create ZIP archives."
		args.add("-ep"); // -EP - exclude paths from names
		args.add("-m" + cfg.getCompressionMethod().getCode()); // -M<n> - set compression method
		args.add("-y"); // -Y - assume Yes on all queries
		if (OverwriteMode.UPDATE == cfg.getTargetOverwriteMode()) // -O[+|-] - set the overwrite mode
		{
			args.add("-o+");
		}
		else
		{
			args.add("-o-");
		}
		if (DeletionMode.DELETE == cfg.getSourceDeletionMode())
		{
			args.add("-df"); // -DF - delete files after archiving
		}

		// target package
		args.add(target.toString());

		// source file
		args.add(source.toString());

		// Unix expects a command list which contains exactly two elements:
		// 1) the executable
		// 2) the argument(s); separated with whitespace
		List<String> command = new ArrayList<>(2);
		command.add(RAR_EXECUTABLE);
		command.add(Joiner.on(' ').join(args));
		return command;
	}
}
