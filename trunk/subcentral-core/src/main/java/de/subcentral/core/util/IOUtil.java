package de.subcentral.core.util;

import java.io.InputStream;
import java.util.Scanner;

public class IOUtil
{
	public static String readInputStream(InputStream is)
	{
		if (is == null)
		{
			return "";
		}
		try (Scanner s = new Scanner(is))
		{
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}

	private IOUtil()
	{
		// utilty class
	}
}
