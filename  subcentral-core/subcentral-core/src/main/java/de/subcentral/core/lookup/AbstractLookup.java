package de.subcentral.core.lookup;

import de.subcentral.core.naming.NamingService;

public abstract class AbstractLookup<R, P> implements Lookup<R, P>
{
	protected final NamingService	queryEntityNamingService;

	public AbstractLookup(NamingService queryEntityNamingService)
	{
		this.queryEntityNamingService = queryEntityNamingService;
	}

	public NamingService getQueryEntityNamingService()
	{
		return queryEntityNamingService;
	}

	@Override
	public LookupQuery<R> createQueryFromEntity(Object queryEntity)
	{
		try
		{
			return createQuery(queryEntityNamingService.name(queryEntity));
		}
		catch (Exception e)
		{
			throw new LookupException(queryEntity, e);
		}
	}

	@Override
	public boolean isQueryEntitySupported(Object queryObject)
	{
		return queryEntityNamingService.canName(queryObject);
	}
}
