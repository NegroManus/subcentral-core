package de.subcentral.core.parsing;

import java.util.List;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

public class SimpleParsingService implements ParsingService
{
	private ListMultimap<Class<?>, Parser<?>>	parsers	= LinkedListMultimap.create();

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
	public Object parse(String text, String domain) throws NoMatchException, ParsingException
	{
		return doParse(text, domain, null);
	}

	@Override
	public <T> T parseTyped(String text, String domain, Class<T> entityType) throws NoMatchException, ParsingException
	{
		try
		{
			return entityType.cast(doParse(text, domain, entityType));
		}
		catch (ClassCastException e)
		{
			throw new ParsingException(text, entityType, e);
		}
	}

	private Object doParse(String text, String domain, Class<?> entityType) throws NoMatchException, ParsingException
	{
		Parsings.requireTextNotBlank(text);
		for (Parser<?> p : entityType == null ? parsers.values() : parsers.get(entityType))
		{
			if (domain == null || p.getDomain().startsWith(domain))
			{
				try
				{
					return p.parse(text);
				}
				catch (NoMatchException e)
				{
					// this parser could no match
					// ignore and move on to the next
				}
			}
		}

		// build Exception message
		StringBuilder msg = new StringBuilder();
		msg.append("No parser ");
		if (domain != null)
		{
			msg.append("with domain '");
			msg.append(domain);
			msg.append("' ");
		}
		if (entityType != null)
		{
			if (domain != null)
			{
				msg.append("and ");
			}
			else
			{
				msg.append("with ");
			}
			msg.append("entity type");
			msg.append(entityType);
			msg.append(' ');
		}
		msg.append("could parse the text");

		throw new NoMatchException(text, entityType, msg.toString());
	}
}
