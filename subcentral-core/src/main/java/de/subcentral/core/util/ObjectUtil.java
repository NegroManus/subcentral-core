package de.subcentral.core.util;

import java.util.regex.Pattern;

public class ObjectUtil
{
	private ObjectUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static boolean equalPatterns(Pattern p1, Pattern p2)
	{
		return (p1 == p2) || (p1 != null && p1.pattern().equals(p2.pattern()) && p1.flags() == p2.flags());
	}
}
