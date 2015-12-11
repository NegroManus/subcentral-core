package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;

public abstract class AbstractPropertySequenceNamer<T> implements Namer<T>
{
	private final PropSequenceNameBuilder.Config config;

	public AbstractPropertySequenceNamer(PropSequenceNameBuilder.Config config)
	{
		this.config = Objects.requireNonNull(config, "config");
	}

	public PropSequenceNameBuilder.Config getConfig()
	{
		return config;
	}

	@Override
	public String name(T obj, Map<String, Object> parameters) throws NamingException
	{
		if (obj == null)
		{
			return "";
		}
		try
		{
			PropSequenceNameBuilder builder = new PropSequenceNameBuilder(config);
			appendName(builder, obj, parameters);
			return builder.toString();
		}
		catch (RuntimeException e)
		{
			throw new NamingException(obj, e);
		}
	}

	protected abstract void appendName(PropSequenceNameBuilder builder, T obj, Map<String, Object> parameters);
}
