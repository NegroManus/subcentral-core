package de.subcentral.support.predbme;

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

public class PreDbMeLookup extends AbstractHttpLookup<Release, String>
{
	@Override
	public String getName()
	{
		return "PreDB.me";
	}

	@Override
	protected URL initHost()
	{
		try
		{
			return new URL("http://www.predb.me/");
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
		return new PreDbMeLookupQuery(initHost()).getResults(doc);
	}

	public Release parseReleaseDetails(File file) throws IOException
	{
		Document doc = Jsoup.parse(file, "UTF-8", initHost().toExternalForm());
		return new PreDbMeLookupQuery(initHost()).parseReleaseDetails(doc, new Release());
	}

	@Override
	public LookupQuery<Release> createQuery(URL query)
	{
		return new PreDbMeLookupQuery(query);
	}

	@Override
	protected String getDefaultQueryPath()
	{
		return "/";
	}

	@Override
	protected String getDefaultQueryPrefix()
	{
		return "search=";
	}

	@Override
	protected URL buildQueryUrlFromParameters(String parameterBean) throws Exception
	{
		return buildDefaultQueryUrl(parameterBean);
	}

}
