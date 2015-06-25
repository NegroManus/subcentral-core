package de.subcentral.core.util;

import java.util.Objects;
import java.util.Set;

import com.google.common.base.MoreObjects;

public class Separation
{
    public static final String DEFAULT_SEPARATOR = " ";

    public static String getSeparatorBetween(SimplePropDescriptor firstProperty, SimplePropDescriptor secondProperty, Set<Separation> separations)
    {
	return getSeparatorBetween(firstProperty, secondProperty, null, separations, DEFAULT_SEPARATOR);
    }

    public static String getSeparatorBetween(SimplePropDescriptor firstProperty, SimplePropDescriptor secondProperty, Set<Separation> separations, String defaultSeparator)
    {
	return getSeparatorBetween(firstProperty, secondProperty, null, separations, defaultSeparator);
    }

    public static String getSeparatorBetween(SimplePropDescriptor firstProperty, SimplePropDescriptor secondProperty, String separationType, Set<Separation> separations)
    {
	return getSeparatorBetween(firstProperty, secondProperty, separationType, separations, DEFAULT_SEPARATOR);
    }

    public static String getSeparatorBetween(SimplePropDescriptor firstProperty, SimplePropDescriptor secondProperty, String separationType, Set<Separation> separations, String defaultSeparator)
    {
	String after = null;
	String before = null;
	String betweenAny = null;
	for (Separation sd : separations)
	{
	    boolean firstPropEquals = Objects.equals(firstProperty, sd.firstProperty);
	    boolean secondPropEquals = Objects.equals(secondProperty, sd.secondProperty);
	    boolean typeEquals = Objects.equals(separationType, sd.type);
	    if (typeEquals)
	    {
		if (firstPropEquals && secondPropEquals)
		{
		    // between / in-between
		    return sd.getSeparator();
		}
		else if (firstPropEquals && sd.secondProperty == null)
		{
		    after = sd.separator;
		}
		else if (sd.firstProperty == null && secondPropEquals)
		{
		    before = sd.separator;
		}
		else if (sd.firstProperty == null && sd.secondProperty == null)
		{
		    betweenAny = sd.separator;
		}
	    }
	}
	if (after != null)
	{
	    return after;
	}
	else if (before != null)
	{
	    return before;
	}
	else if (betweenAny != null)
	{
	    return betweenAny;
	}
	return defaultSeparator;
    }

    private final SimplePropDescriptor firstProperty;
    private final SimplePropDescriptor secondProperty;
    private final String	       type;
    private final String	       separator;

    public static Separation betweenAny(String separator)
    {
	return new Separation(null, null, null, separator);
    }

    public static Separation betweenAny(String type, String separator)
    {
	return new Separation(null, null, type, separator);
    }

    public static Separation after(SimplePropDescriptor property, String separator)
    {
	return new Separation(property, null, null, separator);
    }

    public static Separation after(SimplePropDescriptor property, String type, String separator)
    {
	return new Separation(property, null, type, separator);
    }

    public static Separation before(SimplePropDescriptor property, String separator)
    {
	return new Separation(null, property, null, separator);
    }

    public static Separation before(SimplePropDescriptor property, String type, String separator)
    {
	return new Separation(null, property, type, separator);
    }

    public static Separation inBetween(SimplePropDescriptor property, String separator)
    {
	return new Separation(property, property, null, separator);
    }

    public static Separation inBetween(SimplePropDescriptor property, String type, String separator)
    {
	return new Separation(property, property, type, separator);
    }

    public static Separation between(SimplePropDescriptor firstProperty, SimplePropDescriptor secondProperty, String separator)
    {
	return new Separation(firstProperty, secondProperty, null, separator);
    }

    public static Separation between(SimplePropDescriptor firstProperty, SimplePropDescriptor secondProperty, String type, String separator)
    {
	return new Separation(firstProperty, secondProperty, type, separator);
    }

    private Separation(SimplePropDescriptor firstProperty, SimplePropDescriptor secondProperty, String type, String separator)
    {
	this.firstProperty = firstProperty;
	this.secondProperty = secondProperty;
	this.type = type;
	this.separator = separator == null ? DEFAULT_SEPARATOR : separator;
    }

    public SimplePropDescriptor getFirstProperty()
    {
	return firstProperty;
    }

    public SimplePropDescriptor getSecondProperty()
    {
	return secondProperty;
    }

    public String getType()
    {
	return type;
    }

    public String getSeparator()
    {
	return separator;
    }

    @Override
    public String toString()
    {
	return MoreObjects.toStringHelper(Separation.class)
		.omitNullValues()
		.add("firstProperty", firstProperty)
		.add("secondProperty", secondProperty)
		.add("type", type)
		.add("separator", separator)
		.toString();
    }
}
