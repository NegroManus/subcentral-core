package de.subcentral.core.util;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Scanner;

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
			return null;
		}
		int indexOfLastDot = filename.lastIndexOf('.');
		if (indexOfLastDot == -1)
		{
			return new String[] { filename, "" };
		}
		return new String[] { filename.substring(0, indexOfLastDot), filename.substring(indexOfLastDot, filename.length()) };
	}

	private IOUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

}
