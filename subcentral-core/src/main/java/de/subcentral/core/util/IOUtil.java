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

	private IOUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
