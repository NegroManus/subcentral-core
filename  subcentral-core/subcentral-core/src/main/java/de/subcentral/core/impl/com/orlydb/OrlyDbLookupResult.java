package de.subcentral.core.impl.com.orlydb;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.lookup.LookupResult;
import de.subcentral.core.release.MediaRelease;

public class OrlyDbLookupResult implements LookupResult<MediaRelease>
{
	private final List<MediaRelease>	results;

	public OrlyDbLookupResult()
	{
		results = ImmutableList.of();
	}

	public OrlyDbLookupResult(List<MediaRelease> results)
	{
		this.results = results;
	}

	@Override
	public List<MediaRelease> getResults(int page)
	{
		return results;
	}

	@Override
	public List<MediaRelease> getAllResults()
	{
		return results;
	}

	@Override
	public int getNumberOfResultPages()
	{
		return 1;
	}

}
