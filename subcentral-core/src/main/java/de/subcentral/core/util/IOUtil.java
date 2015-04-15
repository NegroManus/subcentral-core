package de.subcentral.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.MoreObjects;

public class IOUtil
{
	public static final class CommandResult
	{
		private final int		exitValue;
		private final String	logMessage;
		private final String	errorMessage;

		private CommandResult(int exitValue, String logMessage, String errorMessage)
		{
			this.exitValue = exitValue;
			this.logMessage = logMessage;
			this.errorMessage = errorMessage;
		}

		public int getExitValue()
		{
			return exitValue;
		}

		public String getLogMessage()
		{
			return logMessage;
		}

		public String getErrorMessage()
		{
			return errorMessage;
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(CommandResult.class)
					.omitNullValues()
					.add("exitValue", exitValue)
					.add("logMessage", logMessage)
					.add("errorMessage", errorMessage)
					.toString();
		}
	}

	public static CommandResult executeCommand(List<String> command, long timeoutValue, TimeUnit timeoutUnit) throws IOException,
			InterruptedException, TimeoutException
	{
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Process process = processBuilder.start();
		process.getOutputStream().close();

		String errMsg = null;
		String logMsg = null;
		try (InputStream errStream = process.getErrorStream())
		{
			errMsg = StringUtils.stripToNull(IOUtil.readInputStream(errStream));
		}
		try (InputStream logStream = process.getInputStream())
		{
			logMsg = StringUtils.stripToNull(IOUtil.readInputStream(logStream));
		}
		boolean exitedBeforeTimeout = process.waitFor(timeoutValue, timeoutUnit);
		if (!exitedBeforeTimeout)
		{
			throw new TimeoutException("Command execution did not finish before timeout was reached. command=" + command + ", timeout="
					+ timeoutValue + " " + timeoutUnit);
		}
		return new CommandResult(process.exitValue(), logMsg, errMsg);
	}

	public static String readInputStream(InputStream is)
	{
		if (is == null)
		{
			return "";
		}
		try (Scanner s = new Scanner(is, Charset.defaultCharset().name()))
		{
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}

	public static String[] splitIntoFilenameAndExtension(String filename)
	{
		if (filename == null)
		{
			return new String[] { "", "" };
		}
		int indexOfLastDot = filename.lastIndexOf('.');
		if (indexOfLastDot == -1)
		{
			return new String[] { filename, "" };
		}
		return new String[] { filename.substring(0, indexOfLastDot), filename.substring(indexOfLastDot, filename.length()) };
	}

	public static boolean isCompletelyWritten(Path file)
	{
		RandomAccessFile stream = null;
		try
		{
			stream = new RandomAccessFile(file.toFile(), "rw");
			return true;
		}
		catch (Exception e)
		{
			// ignore
			e.printStackTrace();
		}
		finally
		{
			if (stream != null)
			{
				try
				{
					stream.close();
				}
				catch (IOException e)
				{
					// ignore
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public static boolean waitUntilCompletelyWritten(Path file, long timeout, TimeUnit timeoutUnit) throws TimeoutException
	{
		long start = System.currentTimeMillis();
		while (!isCompletelyWritten(file))
		{
			if (System.currentTimeMillis() >= start + timeoutUnit.toMillis(timeout))
			{
				throw new TimeoutException("Timed out after " + timeout + " " + timeoutUnit + " while waiting for file " + file
						+ " to become completely written (unlocked)");
			}
			try
			{
				Thread.sleep(50);
			}
			catch (InterruptedException e)
			{
				// ignore
				e.printStackTrace();
			}
		}
		return true;
	}

	private IOUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

}
