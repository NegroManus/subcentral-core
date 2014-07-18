package de.subcentral.impl.xrel;

import java.util.List;

import de.subcentral.core.lookup.LookupResult;
import de.subcentral.core.model.release.Release;

public class XRelLookupResult implements LookupResult<Release>
{
	private final List<Release>	results;

	public XRelLookupResult(List<Release> results)
	{
		this.results = results;
	}

	@Override
	public List<Release> getResults()
	{
		return results;
	}
}
