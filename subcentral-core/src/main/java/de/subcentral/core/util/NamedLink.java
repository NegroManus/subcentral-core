package de.subcentral.core.util;

import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.Settings;

public class NamedLink implements Comparable<NamedLink>
{
    private final String name;
    private final String link;

    public NamedLink(String link)
    {
	this(link, null);
    }

    public NamedLink(String link, String name)
    {
	this.link = Objects.requireNonNull(link, "link");
	this.name = name;
    }

    public String getLink()
    {
	return link;
    }

    public String getName()
    {
	return name;
    }

    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	{
	    return true;
	}
	if (obj instanceof NamedLink)
	{
	    return link.equalsIgnoreCase(((NamedLink) obj).link);
	}
	return false;
    }

    @Override
    public int hashCode()
    {
	return new HashCodeBuilder(61, 53).append(link.toLowerCase(Locale.ENGLISH)).toHashCode();
    }

    @Override
    public String toString()
    {
	return MoreObjects.toStringHelper(NamedLink.class).omitNullValues().add("link", link).add("name", name).toString();
    }

    @Override
    public int compareTo(NamedLink o)
    {
	// nulls first
	if (o == null)
	{
	    return 1;
	}
	return Settings.STRING_ORDERING.compare(link, o.link);
    }
}
