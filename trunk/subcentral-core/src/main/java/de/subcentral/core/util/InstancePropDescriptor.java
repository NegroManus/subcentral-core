package de.subcentral.core.util;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class InstancePropDescriptor extends SimplePropDescriptor
{
	private final int[]	instanceIds;

	public InstancePropDescriptor(SimplePropDescriptor propDescriptor, int[] instanceIds)
	{
		this(propDescriptor.getBeanClass(), propDescriptor.getPropName(), instanceIds);
	}

	public InstancePropDescriptor(Class<?> beanClass, String propName, int[] instanceIds)
	{
		super(beanClass, propName);
		Objects.requireNonNull(instanceIds, "instanceIds");
		this.instanceIds = instanceIds;
	}

	public int[] getInstanceIds()
	{
		return instanceIds;
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (super.equals(obj) && InstancePropDescriptor.class.equals(obj.getClass()))
		{
			InstancePropDescriptor o = (InstancePropDescriptor) obj;
			return Arrays.equals(instanceIds, o.instanceIds);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(13, 3).appendSuper(super.hashCode()).append(instanceIds).toHashCode();
	}

	@Override
	public String toString()
	{
		return super.toString() + instanceIds.toString();
	}
}