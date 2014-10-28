package de.subcentral.core.lookup;

import de.subcentral.core.naming.DelegatingNamingService;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NamingStandards;
import de.subcentral.core.util.CharReplacer;

public abstract class AbstractLookup<R, P> implements Lookup<R, P>
{
	protected final NamingService	queryEntityNamingService;

	public AbstractLookup()
	{
		this.queryEntityNamingService = initQueryEntityNamingService();
	}

	protected NamingService initQueryEntityNamingService()
	{
		CharReplacer queryEntityCharReplacer = new CharReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", "'´`", " ");
		return new DelegatingNamingService("QueryEntityNamingService", NamingStandards.getDefaultNamingService(), queryEntityCharReplacer);
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
