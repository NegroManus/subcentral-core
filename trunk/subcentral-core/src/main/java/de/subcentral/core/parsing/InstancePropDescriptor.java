package de.subcentral.core.parsing;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jsoup.helper.Validate;

import de.subcentral.core.util.SimplePropDescriptor;

public class InstancePropDescriptor
{
	private final SimplePropDescriptor	propDescriptor;
	private final int[]					instanceIds;

	public InstancePropDescriptor(SimplePropDescriptor propDescriptor)
	{
		this(propDescriptor, ArrayUtils.EMPTY_INT_ARRAY);
	}

	public InstancePropDescriptor(SimplePropDescriptor propDescriptor, int instanceId)
	{
		this(propDescriptor, new int[] { instanceId });
	}

	public InstancePropDescriptor(SimplePropDescriptor propDescriptor, int[] instanceIds)
	{
		Validate.notNull(propDescriptor, "propDescriptor cannot be null");
		Validate.notNull(instanceIds, "instanceIds cannot be null");
		this.propDescriptor = propDescriptor;
		this.instanceIds = instanceIds;
	}

	public SimplePropDescriptor getPropDescriptor()
	{
		return propDescriptor;
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
		if (obj != null && InstancePropDescriptor.class == obj.getClass())
		{
			InstancePropDescriptor o = (InstancePropDescriptor) obj;
			return propDescriptor.equals(o.propDescriptor) && Arrays.equals(instanceIds, instanceIds);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(13, 3).append(propDescriptor).append(instanceIds).toHashCode();
	}

	@Override
	public String toString()
	{
		return propDescriptor + Arrays.toString(instanceIds);
	}
}
