package de.subcentral.core.io.winrar;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import com.google.common.base.MoreObjects;

public class WinRarPackResult
{
	public static final int	EXIT_CODE_SUCCESSFUL							= 0;
	public static final int	EXIT_CODE_WARNING								= 1;
	public static final int	EXIT_CODE_FATAL_ERROR							= 2;
	public static final int	EXIT_CODE_CRC_ERROR								= 3;
	public static final int	EXIT_CODE_ARCHIVE_LOCKED						= 4;
	public static final int	EXIT_CODE_WRITE_ERROR							= 5;
	public static final int	EXIT_CODE_OPEN_ERROR							= 6;
	public static final int	EXIT_CODE_WRONG_OPTION							= 7;
	public static final int	EXIT_CODE_OUT_OF_MEMORY							= 8;
	public static final int	EXIT_CODE_CREATE_ERROR							= 9;
	public static final int	EXIT_CODE_NO_FILES_MATCHING_MASK_AND_OPTIONS	= 10;
	public static final int	EXIT_CODE_WRONG_PASSWORD						= 11;
	public static final int	EXIT_CODE_USER_BREAK							= 255;

	/**
	 * <pre>
	 *  Exit values
	 *  ~~~~~~~~~~~
	 * 
	 *     RAR exits with a zero code (0) in case of successful operation.
	 *     Non-zero exit code indicates some kind of error:
	 * 
	 *     Code   Description   
	 * 
	 *      0     Successful operation.
	 *      1     Non fatal error(s) occurred.
	 *      2     A fatal error occurred.
	 *      3     Invalid checksum. Data is damaged.
	 *      4     Attempt to modify an archive locked by 'k' command.
	 *      5     Write error.
	 *      6     File open error.
	 *      7     Wrong command line option.
	 *      8     Not enough memory.
	 *      9     File create error
	 *     10     No files matching the specified mask and options were found.
	 *     11     Wrong password.
	 *    255     User stopped the process.
	 * </pre>
	 * 
	 * @param exitCode
	 * @return
	 */
	public static String getExitCodeDescription(int exitCode, Locale locale)
	{
		switch (exitCode)
		{
			case EXIT_CODE_SUCCESSFUL:
				return "Successful operation.";
			case EXIT_CODE_WARNING:
				return "Warning. Non fatal error(s) occurred.";
			case EXIT_CODE_FATAL_ERROR:
				return "A fatal error occurred.";
			case EXIT_CODE_CRC_ERROR:
				return "Invalid checksum. Data is damaged.";
			case EXIT_CODE_ARCHIVE_LOCKED:
				return "Attempt to modify an archive locked by 'k' command.";
			case EXIT_CODE_WRITE_ERROR:
				return "Write error.";
			case EXIT_CODE_OPEN_ERROR:
				return "File open error.";
			case EXIT_CODE_WRONG_OPTION:
				return "Wrong command line option.";
			case EXIT_CODE_OUT_OF_MEMORY:
				return "Not enough memory.";
			case EXIT_CODE_CREATE_ERROR:
				return "File create error.";
			case EXIT_CODE_NO_FILES_MATCHING_MASK_AND_OPTIONS:
				return "No files matching the specified mask and options were found.";
			case EXIT_CODE_WRONG_PASSWORD:
				return "Wrong password.";
			case EXIT_CODE_USER_BREAK:
				return "User break.";
			default:
				return "";
		}
	}

	public static enum Flag
	{
		ALREADY_EXISTED, REPLACED, DELETED_SOURCE;
	}

	private final EnumSet<Flag>	flags;
	private final Exception		exception;

	WinRarPackResult()
	{
		this.flags = EnumSet.noneOf(Flag.class);
		this.exception = null;
	}

	WinRarPackResult(EnumSet<Flag> flags)
	{
		this.flags = EnumSet.copyOf(flags);
		this.exception = null;
	}

	WinRarPackResult(Flag first, Flag... rest)
	{
		this.flags = EnumSet.of(first, rest);
		this.exception = null;
	}

	WinRarPackResult(Exception exception)
	{
		this.flags = EnumSet.noneOf(Flag.class);
		this.exception = exception;
	}

	public Set<Flag> getFlags()
	{
		return flags;
	}

	public Exception getException()
	{
		return exception;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(WinRarPackResult.class).omitNullValues().add("flags", flags).add("exception", exception).toString();
	}
}
