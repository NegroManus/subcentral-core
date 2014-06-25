package de.subcentral.core.impl.com.orlydb;

import java.net.URL;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.lookup.LookupResult;
import de.subcentral.core.release.MediaRelease;

public class OrlyDbLookupResult implements LookupResult<MediaRelease>
{
	private final URL					url;
	private final List<MediaRelease>	results;

	public OrlyDbLookupResult(URL url)
	{
		this.url = url;
		this.results = ImmutableList.of();
	}

	public OrlyDbLookupResult(URL url, List<MediaRelease> results)
	{
		this.url = url;
		this.results = results;
	}

	public URL getUrl()
	{
		return url;
	}

	@Override
	public List<MediaRelease> getResults()
	{
		return results;
	}

}
