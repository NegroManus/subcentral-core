package de.subcentral.mig.repo_old;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.name.NamingDefaults;

public class SeriesKey
{
	private final String name;

	public SeriesKey(Series series)
	{
		this(series.getName());
	}

	public SeriesKey(String name)
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
		if (obj instanceof SeriesKey)
		{
			return this.name.equals(((SeriesKey) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(761, 131).append(name).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(SeriesKey.class).add("name", name).toString();
	}
}
