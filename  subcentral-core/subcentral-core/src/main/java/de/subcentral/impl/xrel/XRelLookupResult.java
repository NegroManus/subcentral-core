package de.subcentral.thirdparty.to.xrel;

import java.util.List;

import de.subcentral.core.lookup.LookupResult;
import de.subcentral.core.model.release.MediaRelease;

public class XRelLookupResult implements LookupResult<MediaRelease>
{
	private final List<MediaRelease>	results;

	public XRelLookupResult(List<MediaRelease> results)
	{
		this.results = results;
	}

	@Override
	public List<MediaRelease> getResults()
	{
		return results;
	}
}
