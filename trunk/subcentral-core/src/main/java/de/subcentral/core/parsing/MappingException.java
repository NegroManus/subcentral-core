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

	public MappingException(Map<SimplePropDescriptor, String> props, Class<?> entityClass, String message)
	{
		this(props, entityClass, message, null);
	}

	public MappingException(Map<SimplePropDescriptor, String> props, Class<?> entityClass, Throwable cause)
	{
		this(props, entityClass, "", cause);
	}

	public MappingException(Map<SimplePropDescriptor, String> props, Class<?> entityClass, String message, Throwable cause)
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
			msg.append("; properties=");
			msg.append(props);
		}
		if (entityClass != null)
		{
			msg.append("; entityClass=");
			msg.append(entityClass);
		}
		return msg.toString();
	}
}
