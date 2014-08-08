package de.subcentral.core.parsing;

import org.apache.commons.lang3.StringUtils;

public class Parsings
{
	/**
	 * Pattern for media names like "The Lord of the Rings (2003)", "The Office (UK)".<br/>
	 * Groups
	 * <ol>
	 * <li>name</li>
	 * <li>title (may be equal to name)</li>
	 * <li>(optional year / country group)</li>
	 * <li>(either year or country group)</li>
	 * <li>year (or null)</li>
	 * <li>country code (or null)</li>
	 * </ol>
	 */
	public static final String	PATTERN_MEDIA_NAME	= "((.*?)(\\s+\\(((\\d{4})|(\\p{Upper}{2}))\\))?)";

	public static void requireTextNotBlank(String text) throws ParsingException
	{
		if (StringUtils.isBlank(text))
		{
			throw new ParsingException("Could not parse text because it is blank: " + (text == null ? "null" : "'" + text + "'"));
		}
	}

	private Parsings()
	{
		// util class
	}

}
