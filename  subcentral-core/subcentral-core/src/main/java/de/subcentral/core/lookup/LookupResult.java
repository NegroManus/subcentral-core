package de.subcentral.core.lookup;

import java.util.List;

public interface LookupResult<R>
{
	public default boolean areResultsAvailable()
	{
		return !getResults().isEmpty();
	}

	public default List<R> getResults()
	{
		return getResults(0);
	}

	public List<R> getResults(int page);

	public default R getFirstResult()
	{
		List<R> results = getResults(0);
		if (results.isEmpty())
		{
			return null;
		}
		return results.get(0);
	}

	public List<R> getAllResults();

	public int getNumberOfResultPages();
}
