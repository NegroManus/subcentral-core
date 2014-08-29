package de.subcentral.support.xrel;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.subcentral.core.lookup.AbstractHttpLookup;
import de.subcentral.core.lookup.LookupQuery;
import de.subcentral.core.model.release.Release;

public class XRelLookup extends AbstractHttpLookup<Release, String>
{
	@Override
	public String getName()
	{
		return "xREL";
	}

	@Override
	protected URL initHost()
	{
		try
		{
			return new URL("http://www.xrel.to/");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Class<Release> getResultClass()
	{
		return Release.class;
	}

	@Override
	public Class<String> getParameterBeanClass()
	{
		return String.class;
	}

	public List<Release> parseReleases(File file) throws IOException
	{
		Document doc = Jsoup.parse(file, "UTF-8", initHost().toExternalForm());
		return new XRelLookupQuery(initHost()).getResults(doc);
	}

	@Override
	public LookupQuery<Release> createQuery(URL query)
	{
		return new XRelLookupQuery(query);
	}

	@Override
	protected String getDefaultQueryPrefix()
	{
		// Do not use "mode=rls", because then results are displayed different
		// and the info url is only available via an ajax call.
		return "xrel_search_query=";
	}

	@Override
	protected String getDefaultQueryPath()
	{
		return "/search.html";
	}

	@Override
	protected URL buildQueryUrlFromParameters(String parameterBean) throws Exception
	{
		return buildDefaultQueryUrl(parameterBean);
	}
}
