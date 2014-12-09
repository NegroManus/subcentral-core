package de.subcentral.core.infodb;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.naming.DelegatingNamingService;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NamingStandards;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.PatternReplacer;

public abstract class AbstractInfoDb<R, P> implements InfoDb<R, P>
{
	protected final NamingService	queryObjectNamingService;

	public AbstractInfoDb()
	{
		this.queryObjectNamingService = Objects.requireNonNull(initQueryObjectNamingService(), "queryObjectNamingService");
	}

	protected NamingService initQueryObjectNamingService()
	{
		PatternReplacer pr = new PatternReplacer(ImmutableMap.of(Pattern.compile("&"), "and"));
		CharReplacer cr = new CharReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", "'´`", ' ');
		return new DelegatingNamingService("QueryEntityNamingService", NamingStandards.getDefaultNamingService(), pr.andThen(cr));
	}

	public NamingService getQueryEntityNamingService()
	{
		return queryObjectNamingService;
	}

	@Override
	public List<R> queryWithName(Object obj) throws InfoDbUnavailableException, InfoDbQueryException
	{
		try
		{
			return query(queryObjectNamingService.name(obj));
		}
		catch (InfoDbUnavailableException ue)
		{
			throw ue;
		}
		catch (Exception e)
		{
			throw new InfoDbQueryException(this, obj, e);
		}
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
