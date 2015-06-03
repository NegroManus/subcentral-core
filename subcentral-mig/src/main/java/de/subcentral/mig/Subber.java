package de.subcentral.mig;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.metadata.Contributor;

public class Subber implements Contributor
{
	private final String	name;

	public Subber(String name)
	{
		this.name = name;
	}

	@Override
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
		if (obj instanceof Subber)
		{
			return Objects.equals(name, ((Subber) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(973, 59).append(name).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Subber.class).omitNullValues().add("name", name).toString();
	}
}