package de.subcentral.core.naming;

import java.util.Objects;
import java.util.function.Predicate;

public class NamingFilter<T> implements Predicate<T>
{
	private final NamingService	namingService;
	private final String		expectedName;

	public NamingFilter(NamingService namingService, T expectedObject)
	{
		this.namingService = Objects.requireNonNull(namingService, "namingService");
		this.expectedName = namingService.name(expectedObject);
	}

	@Override
	public boolean test(T o)
	{
		return expectedName.equals(namingService.name(o));
	}
}