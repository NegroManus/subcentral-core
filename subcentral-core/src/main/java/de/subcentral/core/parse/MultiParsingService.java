package de.subcentral.core.parse;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

public class MultiParsingService implements ParsingService
{
	private final String				name;
	private final List<ParsingService>	parsingServices;

	public MultiParsingService(String domain)
	{
		this(domain, ImmutableList.of());
	}

	public MultiParsingService(String name, ParsingService... parsingServices)
	{
		this.name = Objects.requireNonNull(name, "name");
		this.parsingServices = new CopyOnWriteArrayList<>(parsingServices);
	}

	public MultiParsingService(String domain, Collection<ParsingService> parsingServices)
	{
		this.name = Objects.requireNonNull(domain, "name");
		this.parsingServices = new CopyOnWriteArrayList<>(parsingServices);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Set<Class<?>> getSupportedTargetTypes()
	{
		return parsingServices.stream().flatMap((ParsingService ps) -> ps.getSupportedTargetTypes().stream()).collect(Collectors.toSet());
	}

	public List<ParsingService> getParsingServices()
	{
		return parsingServices;
	}

	@Override
	public Object parse(String text)
	{
		for (ParsingService ps : parsingServices)
		{
			Object parsedObj = ps.parse(text);
			if (parsedObj != null)
			{
				return parsedObj;
			}
		}
		return null;
	}

	@Override
	public <T> T parse(String text, Class<T> targetType)
	{
		for (ParsingService ps : parsingServices)
		{
			T parsedObj = ps.parse(text, targetType);
			if (parsedObj != null)
			{
				return parsedObj;
			}
		}
		return null;
	}

	@Override
	public Object parse(String text, Set<Class<?>> targetTypes)
	{
		for (ParsingService ps : parsingServices)
		{
			Object parsedObj = ps.parse(text, targetTypes);
			if (parsedObj != null)
			{
				return parsedObj;
			}
		}
		return null;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(MultiParsingService.class).add("name", name).add("parsingServices", parsingServices).toString();
	}
}
