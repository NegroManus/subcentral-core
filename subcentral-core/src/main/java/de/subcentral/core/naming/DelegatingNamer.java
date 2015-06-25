package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class DelegatingNamer<T> implements Namer<T>
{
    private final Namer<T>		   delegate;
    private final Function<String, String> finalFormatter;

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
    public String name(T candidate, Map<String, Object> parameters) throws NamingException
    {
	return finalFormatter.apply(delegate.name(candidate, parameters));
    }
}
