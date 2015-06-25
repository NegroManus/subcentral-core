package de.subcentral.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil
{
    public static String stackTraceToString(Throwable t)
    {
	StringWriter sw = new StringWriter();
	t.printStackTrace(new PrintWriter(sw));
	return sw.toString();
    }

    private ExceptionUtil()
    {
	throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }
}
