package de.subcentral.core.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jsoup.helper.Validate;

import com.google.common.collect.ComparisonChain;

import de.subcentral.core.Settings;

/**
 * Class for defining a bean / entity property. Unlike {@link PropertyDescriptor} no further checks are conducted. This class is simply a container
 * for a Class value and a String value. <code>SimplePropertyDescriptor</code> instances are not equal just because their write and read methods are
 * equal due to inheritance - like it is the case with <code>PropertyDescriptor</code>.
 *
 */
public class SimplePropDescriptor implements Comparable<SimplePropDescriptor>
{
	private final Class<?>	beanClass;
	private final String	propertyName;

	public SimplePropDescriptor(Class<?> beanClass, String propertyName)
	{
		Validate.notNull(beanClass, "beanClass cannot be null");
		Validate.notNull(propertyName, "propertyName cannot be null");
		this.beanClass = beanClass;
		this.propertyName = propertyName;
	}

	public Class<?> getBeanClass()
	{
		return beanClass;
	}

	public String getPropertyName()
	{
		return propertyName;
	}

	public String getName()
	{
		return beanClass.getName() + "." + propertyName;
	}

	public String getSimpleName()
	{
		return beanClass.getSimpleName() + "." + propertyName;
	}

	public PropertyDescriptor toPropertyDescriptor() throws IntrospectionException
	{
		return new PropertyDescriptor(propertyName, beanClass);
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj != null && SimplePropDescriptor.class == obj.getClass())
		{
			SimplePropDescriptor o = (SimplePropDescriptor) obj;
			return beanClass.equals(o.beanClass) && propertyName.equals(o.propertyName);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(11, 3).append(beanClass).append(propertyName).toHashCode();
	}

	@Override
	public int compareTo(SimplePropDescriptor o)
	{
		if (o == null)
		{
			return -1;
		}
		return ComparisonChain.start()
				.compare(beanClass.getName(), o.beanClass.getName(), Settings.STRING_ORDERING)
				.compare(propertyName, o.propertyName, Settings.STRING_ORDERING)
				.result();
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
