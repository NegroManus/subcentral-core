package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public class MappingException extends RuntimeException
{
	private static final long						serialVersionUID	= -7524198648124655458L;

	private final Map<SimplePropDescriptor, String>	props;
	private final Class<?>							targetClass;

	public MappingException(Map<SimplePropDescriptor, String> props, Class<?> targetClass, String message)
	{
		this(props, targetClass, message, null);
	}

	public MappingException(Map<SimplePropDescriptor, String> props, Class<?> targetClass, Throwable cause)
	{
		this(props, targetClass, "", cause);
	}

	public MappingException(Map<SimplePropDescriptor, String> props, Class<?> targetClass, String message, Throwable cause)
	{
		super(message, cause);
		this.props = props;
		this.targetClass = targetClass;
	}

	public Map<SimplePropDescriptor, String> getProps()
	{
		return props;
	}

	public Class<?> getEntityClass()
	{
		return targetClass;
	}

	@Override
	public String getMessage()
	{
		StringBuilder msg = new StringBuilder();
		msg.append(super.getMessage());
		if (msg.length() > 0)
		{
			msg.append("; ");
		}
		if (props != null)
		{
			msg.append("properties=");
			msg.append(props);
		}
		if (msg.length() > 0)
		{
			msg.append("; ");
		}
		if (targetClass != null)
		{
			msg.append("targetClass=");
			msg.append(targetClass);
		}
		return msg.toString();
	}
}
