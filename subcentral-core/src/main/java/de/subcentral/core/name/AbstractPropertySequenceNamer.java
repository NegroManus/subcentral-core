package de.subcentral.core.name;

import java.util.Objects;

import de.subcentral.core.util.Context;

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
	public String name(T obj, Context ctx)
	{
		if (obj == null)
		{
			return "";
		}
		PropSequenceNameBuilder builder = new PropSequenceNameBuilder(config);
		appendName(builder, obj, ctx);
		return builder.toString();
	}

	protected abstract void appendName(PropSequenceNameBuilder builder, T obj, Context ctx);
}
