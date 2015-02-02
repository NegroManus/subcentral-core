package de.subcentral.core.metadata.db;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.naming.DelegatingNamingService;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.PatternReplacer;

public abstract class AbstractMetadataDb<T> implements MetadataDb<T>
{
	protected final NamingService	queryObjectNamingService;

	public AbstractMetadataDb()
	{
		this.queryObjectNamingService = Objects.requireNonNull(initQueryObjectNamingService(), "queryObjectNamingService");
	}

	protected NamingService initQueryObjectNamingService()
	{
		PatternReplacer pr = new PatternReplacer(ImmutableMap.of(Pattern.compile("&"), "and"));
		CharReplacer cr = new CharReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", "'´`", ' ');
		return new DelegatingNamingService("QueryEntityNamingService", NamingDefaults.getDefaultNamingService(), pr.andThen(cr));
	}

	public NamingService getQueryObjectNamingService()
	{
		return queryObjectNamingService;
	}

	@Override
	public List<T> queryWithName(Object metaDataObj) throws MetadataDbUnavailableException, MetadataDbQueryException
	{
		try
		{
			return query(queryObjectNamingService.name(metaDataObj));
		}
		catch (MetadataDbUnavailableException ue)
		{
			throw ue;
		}
		catch (Exception e)
		{
			throw new MetadataDbQueryException(this, metaDataObj, e);
		}
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
