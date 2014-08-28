package de.subcentral.core.io.winrar;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;

import com.google.common.base.Joiner;

import de.subcentral.core.io.winrar.WinRar.RarExeLocation;

class UnixWinRarPackager extends WinRarPackager
{
	private static final String	RAR_EXE						= "rar";

	private static final String	RESOURCE_RAR_EXE_LINUX_32	= "rar_5.10_linux_x32";
	private static final String	RESOURCE_RAR_EXE_LINUX_64	= "rar_5.10_linux_x64";
	private static final String	RESOURCE_RAR_EXE_MAC_OSX	= "rar_5.10_macosx";
	private static final String	RESOURCE_RAR_EXE_FREE_BSD	= "rar_5.10_freebsd";

	UnixWinRarPackager(RarExeLocation rarExeLocationSpecifier, Path rarExe)
	{
		super(rarExeLocationSpecifier, rarExe);
	}

	@Override
	protected Path loadRarResource() throws URISyntaxException
	{
		if (SystemUtils.IS_OS_LINUX)
		{
			if (SystemUtils.OS_ARCH.contains("64"))
			{
				return loadRarResource(RESOURCE_RAR_EXE_LINUX_64);
			}
			else
			{
				return loadRarResource(RESOURCE_RAR_EXE_LINUX_32);
			}
		}
		else if (SystemUtils.IS_OS_MAC_OSX)
		{
			return loadRarResource(RESOURCE_RAR_EXE_MAC_OSX);
		}
		else if (SystemUtils.IS_OS_FREE_BSD)
		{
			return loadRarResource(RESOURCE_RAR_EXE_FREE_BSD);
		}
		// default
		return loadRarResource(RESOURCE_RAR_EXE_LINUX_32);
	}

	@Override
	protected Path locateRarExecutable()
	{
		return Paths.get(RAR_EXE);
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

		// source file
		args.add(source.toString());

		// unit wants a command list containing exact two elements:
		// 1) the executable
		// 2) the argument(s)
		List<String> command = new ArrayList<>();
		command.add(RAR_EXE);
		command.add(Joiner.on(' ').join(args));
		return command;
	}
}
