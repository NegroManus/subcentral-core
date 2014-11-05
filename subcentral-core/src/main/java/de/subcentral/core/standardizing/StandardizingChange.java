package de.subcentral.core.standardizing;

import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.subcentral.core.util.SimplePropDescriptor;

public class StandardizingChange
{
	private final Object				bean;
	private final SimplePropDescriptor	property;
	private final Object				oldValue;
	private final Object				newValue;

	public StandardizingChange(Object bean, SimplePropDescriptor property, Object oldValue, Object newValue)
	{
		this.bean = Objects.requireNonNull(bean, "bean");
		this.property = Objects.requireNonNull(property);
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public Object getBean()
	{
		return bean;
	}

	public SimplePropDescriptor getProperty()
	{
		return property;
	}

	public Object getOldValue()
	{
		return oldValue;
	}

	public Object getNewValue()
	{
		return newValue;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(StandardizingChange.class)
				.omitNullValues()
				.add("bean", bean)
				.add("property", property)
				.add("oldValue", oldValue)
				.add("newValue", newValue)
				.toString();
	}
}
