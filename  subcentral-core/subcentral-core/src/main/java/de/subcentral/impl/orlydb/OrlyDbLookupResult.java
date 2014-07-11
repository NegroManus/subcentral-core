package de.subcentral.impl.orlydb;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.lookup.LookupResult;
import de.subcentral.core.model.release.MediaRelease;

public class OrlyDbLookupResult implements LookupResult<MediaRelease>
{
	private final List<MediaRelease>	results;

	public OrlyDbLookupResult()
	{
		this.results = ImmutableList.of();
	}

	public OrlyDbLookupResult(List<MediaRelease> results)
	{
		this.results = results;
	}

	@Override
	public List<MediaRelease> getResults()
	{
		return results;
	}

}
