package de.subcentral.impl.orlydb;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.lookup.LookupResult;
import de.subcentral.core.model.release.Release;

public class OrlyDbLookupResult implements LookupResult<Release>
{
	private final List<Release>	results;

	public OrlyDbLookupResult()
	{
		this.results = ImmutableList.of();
	}

	public OrlyDbLookupResult(List<Release> results)
	{
		this.results = results;
	}

	@Override
	public List<Release> getResults()
	{
		return results;
	}

}
