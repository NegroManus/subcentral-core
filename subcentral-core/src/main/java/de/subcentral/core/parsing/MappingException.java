package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public class MappingException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long						serialVersionUID	= -7524198648124655458L;

	private final Map<SimplePropDescriptor, String>	props;
	private final Class<?>							entityClass;

	public MappingException(String message, Map<SimplePropDescriptor, String> props, Class<?> entityClass)
	{
		this(message, null, props, entityClass);
	}

	public MappingException(Throwable cause, Map<SimplePropDescriptor, String> props, Class<?> entityClass)
	{
		this("", cause, props, entityClass);
	}

	public MappingException(String message, Throwable cause, Map<SimplePropDescriptor, String> props, Class<?> entityClass)
	{
		super(message, cause);
		this.props = props;
		this.entityClass = entityClass;
	}

	public Map<SimplePropDescriptor, String> getProps()
	{
		return props;
	}

	public Class<?> getEntityClass()
	{
		return entityClass;
	}

	@Override
	public String getMessage()
	{
		StringBuilder msg = new StringBuilder();
		msg.append(super.getMessage());
		if (props != null)
		{
			msg.append("; ");
			msg.append("properties=");
			msg.append(props);
		}
		if (entityClass != null)
		{
			msg.append("; ");
			msg.append("entityClass=");
			msg.append(entityClass);
		}
		return msg.toString();
	}
}
