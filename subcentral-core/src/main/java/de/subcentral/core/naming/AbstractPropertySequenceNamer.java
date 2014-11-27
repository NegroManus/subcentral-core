package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;

import de.subcentral.core.util.Separation;

public abstract class AbstractPropertySequenceNamer<T> implements Namer<T>
{
	protected final PropToStringService			propToStringService;
	protected final String						defaultSeparator;
	protected final ImmutableSet<Separation>	separations;
	protected final Function<String, String>	finalFormatter;

	protected AbstractPropertySequenceNamer(PropToStringService propToStringService)
	{
		this(propToStringService, null, ImmutableSet.of(), null);
	}

	protected AbstractPropertySequenceNamer(PropToStringService propToStringService, String defaultSeparator)
	{
		this(propToStringService, defaultSeparator, ImmutableSet.of(), null);
	}

	protected AbstractPropertySequenceNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations)
	{
		this(propToStringService, defaultSeparator, separations, null);
	}

	protected AbstractPropertySequenceNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter)
	{
		this.propToStringService = Objects.requireNonNull(propToStringService, "propToStringService");
		this.defaultSeparator = (defaultSeparator == null ? Separation.DEFAULT_SEPARATOR : defaultSeparator);
		this.separations = ImmutableSet.copyOf(separations); // includes null check
		this.finalFormatter = finalFormatter;
	}

	public PropToStringService getPropToStringService()
	{
		return propToStringService;
	}

	public ImmutableSet<Separation> getSeparations()
	{
		return separations;
	}

	public String getDefaultSeparator()
	{
		return defaultSeparator;
	}

	public Function<String, String> getFinalFormatter()
	{
		return finalFormatter;
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
		return new PropSequenceNameBuilder(propToStringService, separations, defaultSeparator, finalFormatter);
	}
}
