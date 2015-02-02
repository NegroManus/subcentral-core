package de.subcentral.core.standardizing;

import java.util.Objects;

import com.google.common.base.MoreObjects;

public class StandardizingChange
{
	private final Object	bean;
	private final String	propertyName;
	private final Object	oldValue;
	private final Object	newValue;

	public StandardizingChange(Object bean, String propertyName, Object oldValue, Object newValue)
	{
		this.bean = Objects.requireNonNull(bean, "bean");
		this.propertyName = Objects.requireNonNull(propertyName, "propertyName");
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public Object getBean()
	{
		return bean;
	}

	public String getPropertyName()
	{
		return propertyName;
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
				.add("bean", bean)
				.add("propertyName", propertyName)
				.add("oldValue", oldValue)
				.add("newValue", newValue)
				.toString();
	}
}
