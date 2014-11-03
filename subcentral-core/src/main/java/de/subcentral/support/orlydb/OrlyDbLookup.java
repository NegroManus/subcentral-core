package de.subcentral.support.orlydb;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import de.subcentral.core.lookup.AbstractHttpLookup;
import de.subcentral.core.lookup.LookupQuery;
import de.subcentral.core.model.release.Release;

public class OrlyDbLookup extends AbstractHttpLookup<Release, OrlyDbLookupParameters>
{
	@Override
	public String getName()
	{
		return "ORLYDB";
	}

	@Override
	protected URL initHost()
	{
		try
		{
			return new URL("http://www.orlydb.com/");
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
	public Class<OrlyDbLookupParameters> getParameterBeanClass()
	{
		return OrlyDbLookupParameters.class;
	}

	@Override
	public LookupQuery<Release> createQuery(URL query)
	{
		return new OrlyDbLookupQuery(query);
	}

	@Override
	protected String getDefaultQueryPath()
	{
		return "/";
	}

	@Override
	protected String getDefaultQueryPrefix()
	{
		return "q=";
	}

	@Override
	protected URL buildQueryUrlFromParameters(OrlyDbLookupParameters parameterBean) throws Exception
	{
		if (parameterBean == null)
		{
			return null;
		}
		StringBuilder path = new StringBuilder("/");
		if (!StringUtils.isBlank(parameterBean.getSection()))
		{
			path.append("s/");
			path.append(parameterBean.getSection());
		}
		return buildQueryUrl(path.toString(), getDefaultQueryPrefix(), parameterBean.getQuery());
	}

}
