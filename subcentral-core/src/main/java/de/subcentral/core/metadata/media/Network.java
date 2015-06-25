package de.subcentral.core.metadata.media;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.Settings;

public class Network implements Comparable<Network>
{
    private String name;

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
	    return StringUtils.equalsIgnoreCase(name, ((Network) obj).name);
	}
	return false;
    }

    @Override
    public int hashCode()
    {
	return new HashCodeBuilder(73, 897).append(StringUtils.lowerCase(name, Locale.ENGLISH)).toHashCode();
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
	return MoreObjects.toStringHelper(Network.class).omitNullValues().add("name", name).toString();
    }
}
