package de.subcentral.core.lookup;

import java.util.List;

public interface LookupResult<R>
{
	public default boolean areResultsAvailable()
	{
		return !getResults().isEmpty();
	}

	public List<R> getResults();
}
