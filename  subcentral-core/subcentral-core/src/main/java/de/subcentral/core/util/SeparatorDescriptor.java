package de.subcentral.core.util;

import java.beans.PropertyDescriptor;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class SeparatorDescriptor
{
	public static String getSeparatorFor(PropertyDescriptor firstProperty, PropertyDescriptor secondProperty,
			Set<SeparatorDescriptor> separatorDescriptors)
	{
		SeparatorDescriptor betweenFirstAndAnything = null;
		SeparatorDescriptor betweenAnythingAndSecond = null;
		SeparatorDescriptor betweenAnything = null;
		for (SeparatorDescriptor sd : separatorDescriptors)
		{
			boolean firstPropEquals = firstProperty.equals(sd.getFirstProperty());
			boolean secondPropEquals = secondProperty.equals(sd.getSecondProperty());
			if (firstPropEquals && secondPropEquals)
			{
				return sd.getSeparator();
			}
			else if (firstPropEquals && sd.getSecondProperty() == null)
			{
				betweenFirstAndAnything = sd;
			}
			else if (sd.getFirstProperty() == null && secondPropEquals)
			{
				betweenAnythingAndSecond = sd;
			}
			else if (sd.getFirstProperty() == null && sd.getSecondProperty() == null)
			{
				betweenAnything = sd;
			}
		}
		if (betweenFirstAndAnything != null)
		{
			return betweenFirstAndAnything.getSeparator();
		}
		else if (betweenAnythingAndSecond != null)
		{
			return betweenAnythingAndSecond.getSeparator();
		}
		else if (betweenAnything != null)
		{
			return betweenAnything.getSeparator();
		}
		return null;
	}

	private String				separator;
	private PropertyDescriptor	firstProperty;
	private PropertyDescriptor	secondProperty;

	public SeparatorDescriptor()
	{
		this("", null, null);
	}

	public SeparatorDescriptor(String separator)
	{
		this(separator, null, null);
	}

	public SeparatorDescriptor(String separator, PropertyDescriptor firstProperty)
	{
		this(separator, firstProperty, null);
	}

	public SeparatorDescriptor(String separator, PropertyDescriptor firstProperty, PropertyDescriptor secondProperty)
	{
		this.separator = StringUtils.defaultString(separator);
		this.firstProperty = firstProperty;
		this.secondProperty = secondProperty;
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
