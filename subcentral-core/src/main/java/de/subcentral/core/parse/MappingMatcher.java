package de.subcentral.core.parse;

import java.util.Map;

public interface MappingMatcher<K>
{
	/**
	 * 
	 * @param text
	 *            the text to
	 * @return the mapped groups. If the matcher does no match, an empty map is returned
	 */
	public Map<K, String> match(String text);
}