package de.subcentral.core.lookup;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class EmptyLookupResult<R> implements LookupResult<R>
{

	@Override
	public boolean areResultsAvailable()
	{
		return false;
	}

	@Override
	public List<R> getResults(int page)
	{
		return ImmutableList.of();
	}

	@Override
	public List<R> getAllResults()
	{
		return ImmutableList.of();
	}

	@Override
	public int getNumberOfResultPages()
	{
		return 0;
	}

}
