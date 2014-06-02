package de.subcentral.core.util;

import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class StringUtil
{

	public static final Joiner		COMMA_JOINER		= Joiner.on(',').skipNulls();
	public static final Splitter	COMMA_SPLITTER		= Splitter.on(',');

	public static final Splitter	WHITESPACE_SPLITTER	= Splitter.on(Pattern.compile("\\s+"));

	private StringUtil()
	{
		// static utility class
	}

}
