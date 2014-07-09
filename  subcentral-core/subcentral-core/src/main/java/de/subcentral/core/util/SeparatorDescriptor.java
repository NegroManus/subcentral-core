package de.subcentral.core.util;

import java.beans.PropertyDescriptor;
import java.util.Objects;
import java.util.Set;

public class SeparatorDescriptor
{
	public static final String	DEFAULT_SEPARATOR	= " ";

	public static String getSeparatorBetween(PropertyDescriptor firstProperty, PropertyDescriptor secondProperty,
			Set<SeparatorDescriptor> separatorDescriptors)
	{
		String after = null;
		String before = null;
		String betweenAny = null;
		for (SeparatorDescriptor sd : separatorDescriptors)
		{
			boolean firstPropEquals = Objects.equals(firstProperty, sd.getFirstProperty());
			boolean secondPropEquals = Objects.equals(secondProperty, sd.getSecondProperty());
			if (firstPropEquals && secondPropEquals)
			{
				// between / in-between
				return sd.getSeparator();
			}
			else if (firstPropEquals && sd.getSecondProperty() == null)
			{
				after = sd.getSeparator();
			}
			else if (sd.getFirstProperty() == null && secondPropEquals)
			{
				before = sd.getSeparator();
			}
			else if (sd.getFirstProperty() == null && sd.getSecondProperty() == null)
			{
				betweenAny = sd.getSeparator();
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
		return DEFAULT_SEPARATOR;
	}

	private PropertyDescriptor	firstProperty;
	private PropertyDescriptor	secondProperty;
	private String				separator;

	public static SeparatorDescriptor betweenAny(String separator)
	{
		return new SeparatorDescriptor(null, null, separator);
	}

	public static SeparatorDescriptor after(PropertyDescriptor property, String separator)
	{
		return new SeparatorDescriptor(property, null, separator);
	}

	public static SeparatorDescriptor before(PropertyDescriptor property, String separator)
	{
		return new SeparatorDescriptor(null, property, separator);
	}

	public static SeparatorDescriptor inBetween(PropertyDescriptor property, String separator)
	{
		return new SeparatorDescriptor(property, property, separator);
	}

	public static SeparatorDescriptor between(PropertyDescriptor firstProperty, PropertyDescriptor secondProperty, String separator)
	{
		return new SeparatorDescriptor(firstProperty, secondProperty, separator);
	}

	private SeparatorDescriptor(PropertyDescriptor firstProperty, PropertyDescriptor secondProperty, String separator)
	{
		this.firstProperty = firstProperty;
		this.secondProperty = secondProperty;
		this.separator = separator == null ? DEFAULT_SEPARATOR : separator;
	}

	public String getSeparator()
	{
		return separator;
	}

	public void setSeparator(String separator)
	{
		this.separator = separator;
	}

	public PropertyDescriptor getFirstProperty()
	{
		return firstProperty;
	}

	public void setFirstProperty(PropertyDescriptor firstProperty)
	{
		this.firstProperty = firstProperty;
	}

	public PropertyDescriptor getSecondProperty()
	{
		return secondProperty;
	}

	public void setSecondProperty(PropertyDescriptor secondProperty)
	{
		this.secondProperty = secondProperty;
	}
}
