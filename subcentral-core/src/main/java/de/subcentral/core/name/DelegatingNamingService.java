package de.subcentral.core.name;

import java.util.Objects;
import java.util.function.Function;

import de.subcentral.core.util.Context;

public class DelegatingNamingService implements NamingService
{
	private final String					name;
	private final NamingService				delegate;
	private final Function<String, String>	finalFormatter;

	public DelegatingNamingService(String name, NamingService delegate, Function<String, String> finalFormatter)
	{
		this.name = Objects.requireNonNull(name, "name");
		this.delegate = Objects.requireNonNull(delegate, "delegate");
		this.finalFormatter = Objects.requireNonNull(finalFormatter, "finalFormatter");
	}

	@Override
	public String getName()
	{
		return name;
	}

	public NamingService getDelegate()
	{
		return delegate;
	}

	public Function<String, String> getFinalFormatter()
	{
		return finalFormatter;
	}

	@Override
	public String name(Object obj, Context ctx)
	{
		return finalFormatter.apply(delegate.name(obj, ctx));
	}
}
