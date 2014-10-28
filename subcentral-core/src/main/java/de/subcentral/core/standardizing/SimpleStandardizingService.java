package de.subcentral.core.standardizing;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

public class SimpleStandardizingService implements StandardizingService
{
	private final String							domain;
	private ListMultimap<Class<?>, Standardizer<?>>	standardizers	= LinkedListMultimap.create();

	public SimpleStandardizingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	public void setStandardizers(ListMultimap<Class<?>, Standardizer<?>> standardizers)
	{
		this.standardizers = standardizers;
	}

	public ListMultimap<Class<?>, Standardizer<?>> getStandardizers()
	{
		return standardizers;
	}

	public <T> boolean registerStandardizer(Class<T> entityType, Standardizer<T> standardizer)
	{
		return standardizers.put(entityType, standardizer);
	}

	public <T> boolean registerAllStandardizers(Class<T> entityType, Iterable<Standardizer<T>> standardizers)
	{
		return this.standardizers.putAll(entityType, standardizers);
	}

	public <T> boolean unregisterStandardizer(Class<T> entityType, Standardizer<T> standardizer)
	{
		return standardizers.remove(entityType, standardizer);
	}

	public <T> List<Standardizer<?>> unregisterAllStandardizers(Class<T> entityType)
	{
		return standardizers.removeAll(entityType);
	}

	@Override
	public void standardize(Object entity)
	{
		doStandardize(entity);
	}

	@SuppressWarnings("unchecked")
	private <T> void doStandardize(T entity)
	{
		if (entity != null)
		{
			for (Standardizer<?> std : standardizers.get(entity.getClass()))
			{
				((Standardizer<T>) std).standardize(entity);
			}
		}
	}
}
