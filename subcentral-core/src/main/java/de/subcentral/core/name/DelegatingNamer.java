package de.subcentral.core.name;

import java.util.Objects;
import java.util.function.Function;

import de.subcentral.core.util.Context;

public class DelegatingNamer<T> implements Namer<T>
{
	private final Namer<T>					delegate;
	private final Function<String, String>	finalFormatter;

	public DelegatingNamer(Namer<T> delegate, Function<String, String> finalFormatter)
	{
		this.delegate = Objects.requireNonNull(delegate, "delegate");
		this.finalFormatter = Objects.requireNonNull(finalFormatter, "finalFormatter");
	}

	public Namer<T> getDelegate()
	{
		return delegate;
	}

	public Function<String, String> getFinalFormatter()
	{
		return finalFormatter;
	}

	@Override
	public String name(T obj, Context ctx)
	{
		return finalFormatter.apply(delegate.name(obj, ctx));
	}
}
