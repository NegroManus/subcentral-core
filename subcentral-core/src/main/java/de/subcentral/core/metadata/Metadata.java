package de.subcentral.core.metadata;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ListMultimap;

public interface Metadata extends Serializable
{
	/**
	 * 
	 * @return the ids (source id -> id)
	 * @see de.subcentral.support.StandardSites
	 */
	public Map<String, String> getIds();

	/**
	 * Additional attributes that have no designated property can be stored in the ListMultimap of getAttributes().
	 * 
	 * @return the additional attributes of this media
	 */
	public ListMultimap<String, Object> getAttributes();

	// Convenience
	@SuppressWarnings("unchecked")
	public default <T> T getAttributeValue(String key) throws ClassCastException
	{
		List<Object> values = getAttributes().get(key);
		return values.isEmpty() ? null : (T) values.get(0);
	}

	@SuppressWarnings("unchecked")
	public default <T> List<T> getAttributeValues(String key) throws ClassCastException
	{
		return (List<T>) getAttributes().get(key);
	}
}
