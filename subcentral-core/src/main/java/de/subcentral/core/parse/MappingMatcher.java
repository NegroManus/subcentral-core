package de.subcentral.core.parse;

import java.util.Map;

public interface MappingMatcher<K>
{
	/**
	 * 
	 * @param text
	 *            the text to
	 * @return the mapped groups. If the matcher does no match, an empty map is returned
	 * @throws IndexOutOfBoundsException
	 *             if there is no capturing group in the {@link #getPattern() pattern} associated with an index specified in {@link #getGroups()}.
	 */
	public Map<K, String> match(String text) throws IndexOutOfBoundsException;
}