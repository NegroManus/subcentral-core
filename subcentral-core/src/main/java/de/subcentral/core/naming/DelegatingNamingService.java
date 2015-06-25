package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class DelegatingNamingService implements NamingService
{
    private final String		   domain;
    private final NamingService		   delegate;
    private final Function<String, String> finalFormatter;

    public DelegatingNamingService(String domain, NamingService delegate, Function<String, String> finalFormatter)
    {
	this.domain = Objects.requireNonNull(domain, "domain");
	this.delegate = Objects.requireNonNull(delegate, "delegate");
	this.finalFormatter = Objects.requireNonNull(finalFormatter, "finalFormatter");
    }

    @Override
    public String getDomain()
    {
	return domain;
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
    public String getDefaultSeparator()
    {
	return delegate.getDefaultSeparator();
    }

    @Override
    public String name(Object candidate, Map<String, Object> parameters) throws NamingException
    {
	return finalFormatter.apply(delegate.name(candidate, parameters));
    }

}
