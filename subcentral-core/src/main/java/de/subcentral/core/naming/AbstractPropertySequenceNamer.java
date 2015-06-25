package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;

public abstract class AbstractPropertySequenceNamer<T> implements Namer<T>
{
    protected final PropSequenceNameBuilder.Config config;

    public AbstractPropertySequenceNamer(PropSequenceNameBuilder.Config config)
    {
	this.config = Objects.requireNonNull(config, "config");
    }

    public PropSequenceNameBuilder.Config getConfig()
    {
	return config;
    }

    @Override
    public String name(T candidate, Map<String, Object> parameters) throws NamingException
    {
	if (candidate == null)
	{
	    return "";
	}
	try
	{
	    PropSequenceNameBuilder builder = builder();
	    buildName(builder, candidate, parameters);
	    return builder.toString();
	}
	catch (RuntimeException e)
	{
	    throw new NamingException(candidate, e);
	}
    }

    public abstract void buildName(PropSequenceNameBuilder b, T candidate, Map<String, Object> parameters);

    private PropSequenceNameBuilder builder()
    {
	return new PropSequenceNameBuilder(config);
    }
}
