package de.subcentral.core.metadata;

import java.io.Serializable;
import java.util.Map;

public interface Metadata extends Serializable
{
	/**
	 * 
	 * @return the ids (source id -> id)
	 * @see de.subcentral.support.StandardSources
	 */
	public Map<String, String> getIds();
}
