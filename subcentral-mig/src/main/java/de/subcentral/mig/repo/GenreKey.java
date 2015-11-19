package de.subcentral.mig.repo;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.metadata.media.Network;
import de.subcentral.core.naming.NamingDefaults;

public class GenreKey
{
	private final String name;

	public GenreKey(Network network)
	{
		this(network.getName());
	}

	public GenreKey(String name)
	{
		this.name = NamingDefaults.getDefaultNormalizingFormatter().apply(name);
	}

	public String getName()
	{
		return name;
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof GenreKey)
		{
			return this.name.equals(((GenreKey) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(71, 967).append(name).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(GenreKey.class).add("name", name).toString();
	}
}