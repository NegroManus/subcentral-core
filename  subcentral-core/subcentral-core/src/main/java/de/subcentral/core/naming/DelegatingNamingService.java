package de.subcentral.core.naming;

import java.util.Map;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.Validate;

public class DelegatingNamingService implements NamingService
{
	private final NamingService			delegate;
	private final UnaryOperator<String>	wholeNameOperator;

	public DelegatingNamingService(NamingService delegate, UnaryOperator<String> wholeNameOperator)
	{
		Validate.notNull(delegate, "delegate");
		Validate.notNull(wholeNameOperator, "wholeNameOperator");
		this.delegate = delegate;
		this.wholeNameOperator = wholeNameOperator;
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
		return delegate.getDomain();
	}

	@Override
	public boolean canName(Object candidate)
	{
		return delegate.canName(candidate);
	}

	@Override
	public <T> String name(T candidate, Map<String, Object> parameters) throws NamingException
	{
		return wholeNameOperator.apply(delegate.name(candidate, parameters));
	}

}
