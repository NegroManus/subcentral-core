package de.subcentral.core.metadata.media;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.BeanUtil;
import de.subcentral.core.PropNames;
import de.subcentral.core.Settings;
import de.subcentral.core.metadata.MetadataBase;
import de.subcentral.core.util.SimplePropDescriptor;

public class Network extends MetadataBase implements Comparable<Network>
{
	private static final long					serialVersionUID	= 6231096307092943124L;

	public static final SimplePropDescriptor	PROP_NAME			= new SimplePropDescriptor(Network.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_IDS			= new SimplePropDescriptor(Network.class, PropNames.IDS);
	private String								name;

	public Network()
	{

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
		return new HashCodeBuilder(73, 897).append(name).toHashCode();
	}

	@Override
	public int compareTo(Network o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return Settings.STRING_ORDERING.compare(name, o.name);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Network.class).omitNullValues().add("name", name).add("ids", BeanUtil.nullIfEmpty(ids)).toString();
	}
}
