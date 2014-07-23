package de.subcentral.impl.predb;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.subcentral.core.lookup.AbstractHttpLookup;
import de.subcentral.core.lookup.LookupQuery;
import de.subcentral.core.model.release.Release;

public class PreDbLookup extends AbstractHttpLookup<Release, String>
{
	public PreDbLookup()
	{
		super(PreDb.getPreDbQueryEntityNamingService());
	}

	@Override
	public String getName()
	{
		return PreDb.NAME;
	}

	@Override
	protected URL getHost()
	{
		return PreDb.HOST_URL;
	}

	@Override
	public Class<Release> getResultClass()
	{
		return Release.class;
	}

	public List<Release> getResults(File file) throws IOException
	{
		Document doc = Jsoup.parse(file, "UTF-8", getHost().toExternalForm());
		return new PreDbLookupQuery(getHost()).getResults(doc);
	}

	@Override
	public LookupQuery<Release> createQuery(URL query)
	{
		return new PreDbLookupQuery(query);
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

	@Override
	public Class<String> getParameterBeanClass()
	{
		return String.class;
	}
}
