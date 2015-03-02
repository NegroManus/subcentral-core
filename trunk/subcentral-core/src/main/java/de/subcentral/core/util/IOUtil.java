package de.subcentral.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.ArrayUtils;

public class IOUtil
{
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
			return ArrayUtils.EMPTY_STRING_ARRAY;
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
