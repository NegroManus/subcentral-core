package de.subcentral.core.metadata;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ListMultimap;

public interface Metadata extends Serializable {
	/**
	 * 
	 * @return the ids (site -> id)
	 * @see de.subcentral.support.StandardSites
	 */
	public Map<Site, String> getIds();

	/**
	 * Additional attributes, that have no designated property, can be stored in the ListMultimap of getAttributes().
	 * 
	 * @return the additional attributes of this metadata
	 */
	public ListMultimap<String, Object> getAttributes();

	// Convenience
	public default String getId(Site site) {
		return getIds().get(site);
	}

	public default String setId(Site site, String id) {
		return getIds().put(site, id);
	}

	@SuppressWarnings("unchecked")
	public default <T> T getFirstAttributeValue(String key) throws ClassCastException {
		List<Object> values = getAttributes().get(key);
		return values.isEmpty() ? null : (T) values.get(0);
	}

	@SuppressWarnings("unchecked")
	public default <T> List<T> getAttributeValues(String key) throws ClassCastException {
		return (List<T>) getAttributes().get(key);
	}

	public default boolean addAttributeValue(String key, Object value) {
		return getAttributes().put(key, value);
	}

	public default boolean addAttributeValues(String key, Iterable<? extends Object> values) {
		return getAttributes().putAll(key, values);
	}

	@SuppressWarnings("unchecked")
	public default <T> List<T> setAttributeValues(String key, Iterable<T> values) {
		return (List<T>) getAttributes().replaceValues(key, values);
	}
}
