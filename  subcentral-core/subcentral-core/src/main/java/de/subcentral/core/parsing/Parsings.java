package de.subcentral.core.parsing;

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

	private Parsings()
	{
		// util class
	}

}
