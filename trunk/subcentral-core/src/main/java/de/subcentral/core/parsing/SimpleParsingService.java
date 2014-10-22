package de.subcentral.core.parsing;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

public class SimpleParsingService implements ParsingService
{
	private final String						domain;
	private ListMultimap<Class<?>, Parser<?>>	parsers	= LinkedListMultimap.create();

	public SimpleParsingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	public ListMultimap<Class<?>, Parser<?>> getParsers()
	{
		return parsers;
	}

	public void setParsers(ListMultimap<Class<?>, Parser<?>> parsers)
	{
		this.parsers = parsers;
	}

	public <T> boolean registerParser(Class<T> entityType, Parser<T> standardizer)
	{
		return parsers.put(entityType, standardizer);
	}

	public <T> boolean registerAllParsers(Class<T> entityType, Iterable<Parser<T>> standardizers)
	{
		return this.parsers.putAll(entityType, standardizers);
	}

	public <T> boolean unregisterParser(Class<T> entityType, Parser<T> standardizer)
	{
		return parsers.remove(entityType, standardizer);
	}

	public <T> List<Parser<?>> unregisterAllParsers(Class<T> entityType)
	{
		return parsers.removeAll(entityType);
	}

	@Override
	public Object parse(String text) throws NoMatchException, ParsingException
	{
		return parseTyped(text, null);
	}

	@Override
	public <T> T parseTyped(String text, Class<T> entityType) throws NoMatchException, ParsingException
	{
		Parsings.requireTextNotBlank(text);
		for (Parser<?> p : entityType == null ? parsers.values() : parsers.get(entityType))
		{
			try
			{
				return entityType.cast(p.parse(text));
			}
			catch (NoMatchException e)
			{
				// this parser could no match
				// ignore and move on to the next
			}
			catch (ClassCastException e)
			{
				throw new ParsingException(text, entityType, e);
			}
		}

		// build Exception message
		StringBuilder msg = new StringBuilder();
		msg.append("No parser ");
		if (entityType != null)
		{
			msg.append("with ");
			msg.append("entity type");
			msg.append(entityType);
			msg.append(' ');
		}
		msg.append("could parse the text");

		throw new NoMatchException(text, entityType, msg.toString());
	}
}
