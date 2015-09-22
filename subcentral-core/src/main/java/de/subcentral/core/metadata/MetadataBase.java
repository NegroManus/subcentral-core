package de.subcentral.core.metadata;

import java.util.HashMap;
import java.util.Map;

public class MetadataBase implements Metadata
{
	protected final Map<String, String> ids = new HashMap<>(2);

	@Override
	public Map<String, String> getIds()
	{
		return ids;
	}

}
