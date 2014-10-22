package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class DelegatingNamingService implements NamingService
{
	private final String				domain;
	private final NamingService			delegate;
	private final UnaryOperator<String>	wholeNameOperator;

	public DelegatingNamingService(String domain, NamingService delegate, UnaryOperator<String> wholeNameOperator)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
		this.delegate = Objects.requireNonNull(delegate, "delegate");
		this.wholeNameOperator = Objects.requireNonNull(wholeNameOperator, "wholeNameOperator");
	}

	public NamingService getDelegate()
	{
		return delegate;
	}

	public UnaryOperator<String> getWholeNameOperator()
	{
		return wholeNameOperator;
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	@Override
	public boolean canName(Object candidate)
	{
		return delegate.canName(candidate);
	}

	@Override
	public String name(Object candidate, Map<String, Object> parameters) throws NamingException
	{
		return wholeNameOperator.apply(delegate.name(candidate, parameters));
	}
}
