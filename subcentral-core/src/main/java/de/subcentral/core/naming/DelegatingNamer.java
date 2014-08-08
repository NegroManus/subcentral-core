package de.subcentral.core.naming;

import java.util.Map;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.Validate;

public class DelegatingNamer<T> implements Namer<T>
{
	private final Namer<T>				delegate;
	private final UnaryOperator<String>	wholeNameOperator;

	public DelegatingNamer(Namer<T> delegate, UnaryOperator<String> wholeNameOperator)
	{
		Validate.notNull(delegate, "delegate");
		Validate.notNull(wholeNameOperator, "wholeNameOperator");
		this.delegate = delegate;
		this.wholeNameOperator = wholeNameOperator;
	}

	@Override
	public String name(T candidate, Map<String, Object> parameters) throws NamingException
	{
		return wholeNameOperator.apply(delegate.name(candidate, parameters));
	}
}
