package de.subcentral.core.metadata.media;

import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.subcentral.core.PropNames;
import de.subcentral.core.metadata.MetadataBase;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.core.util.SimplePropDescriptor;

public class Network extends MetadataBase implements Comparable<Network>
{
	private static final long					serialVersionUID	= 6231096307092943124L;

	public static final SimplePropDescriptor	PROP_NAME			= new SimplePropDescriptor(Network.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_IDS			= new SimplePropDescriptor(Network.class, PropNames.IDS);

	private String								name;

	public Network()
	{
		// default constructor
	}

	public Network(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof Network)
		{
			return Objects.equals(name, ((Network) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(Network.class, name);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Network.class).omitNullValues().add("name", name).add("ids", ObjectUtil.nullIfEmpty(ids)).toString();
	}

	@Override
	public int compareTo(Network o)
	{
		if (this == o)
		{
			return 0;
		}
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return ObjectUtil.getDefaultStringOrdering().compare(name, o.name);
	}
}
