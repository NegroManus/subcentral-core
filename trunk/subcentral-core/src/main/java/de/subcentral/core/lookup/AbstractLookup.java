package de.subcentral.core.lookup;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.naming.DelegatingNamingService;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NamingStandards;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.PatternReplacer;

public abstract class AbstractLookup<R, P> implements Lookup<R, P>
{
	protected final NamingService	queryEntityNamingService;

	public AbstractLookup()
	{
		this.queryEntityNamingService = initQueryEntityNamingService();
	}

	protected NamingService initQueryEntityNamingService()
	{
		PatternReplacer pr = new PatternReplacer(ImmutableMap.of(Pattern.compile("&"), "and"));
		CharReplacer cr = new CharReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", "'´`", ' ');
		return new DelegatingNamingService("QueryEntityNamingService", NamingStandards.getDefaultNamingService(), pr.andThen(cr));
	}

	public NamingService getQueryEntityNamingService()
	{
		return queryEntityNamingService;
	}

	@Override
	public List<R> queryWithQueryObject(Object queryEntity) throws LookupException
	{
		try
		{
			return query(queryEntityNamingService.name(queryEntity));
		}
		catch (Exception e)
		{
			throw new LookupException(queryEntity, e);
		}
	}

	@Override
	public boolean isQueryObjectSupported(Object queryObject)
	{
		return queryEntityNamingService.canName(queryObject);
	}
}
