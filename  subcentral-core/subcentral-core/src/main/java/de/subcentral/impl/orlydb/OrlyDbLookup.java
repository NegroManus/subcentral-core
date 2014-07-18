package de.subcentral.impl.orlydb;

import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import de.subcentral.core.lookup.AbstractHttpLookup;
import de.subcentral.core.lookup.LookupQuery;
import de.subcentral.core.model.release.Release;

public class OrlyDbLookup extends AbstractHttpLookup<Release, OrlyDbLookupParameters>
{
	public OrlyDbLookup()
	{
		super(OrlyDb.getOrlyDbQueryEntityNamingService());
	}

	@Override
	public String getName()
	{
		return OrlyDb.NAME;
	}

	@Override
	protected URL getHost()
	{
		return OrlyDb.HOST_URL;
	}

	@Override
	public Class<Release> getResultClass()
	{
		return Release.class;
	}

	@Override
	public LookupQuery<Release> createQuery(URL query)
	{
		return new OrlyDbLookupQuery(this, query);
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

	@Override
	public Class<OrlyDbLookupParameters> getParameterBeanClass()
	{
		return OrlyDbLookupParameters.class;
	}
}
