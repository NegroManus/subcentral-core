package de.subcentral.core.metadata;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

public class MetadataBase implements Metadata
{
	private static final long						serialVersionUID	= -5801764606343001L;

	protected final Map<String, String>				ids					= new HashMap<>(2);
	protected final ListMultimap<String, Object>	attributes			= LinkedListMultimap.create(0);

	@Override
	public Map<String, String> getIds()
	{
		return ids;
	}

	public void setIds(Map<String, String> ids)
	{
		this.ids.clear();
		this.ids.putAll(ids);
	}

	@Override
	public ListMultimap<String, Object> getAttributes()
	{
		return attributes;
	}

	public void setAttributes(ListMultimap<String, Object> attributes)
	{
		this.attributes.clear();
		this.attributes.putAll(attributes);
	}
}
