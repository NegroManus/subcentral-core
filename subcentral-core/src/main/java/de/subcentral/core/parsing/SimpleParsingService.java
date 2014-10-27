package de.subcentral.core.parsing;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

public class SimpleParsingService implements ParsingService
{
	private final String							domain;
	private final ListMultimap<Class<?>, Parser<?>>	parsers	= LinkedListMultimap.create();

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

	public <T> boolean registerParser(Class<T> targetClass, Parser<T> standardizer)
	{
		return parsers.put(targetClass, standardizer);
	}

	public <T> boolean registerAllParsers(Class<T> targetClass, Iterable<Parser<T>> standardizers)
	{
		return this.parsers.putAll(targetClass, standardizers);
	}

	public <T> boolean unregisterParser(Class<T> targetClass, Parser<T> standardizer)
	{
		return parsers.remove(targetClass, standardizer);
	}

	public <T> List<Parser<?>> unregisterAllParsers(Class<T> targetClass)
	{
		return parsers.removeAll(targetClass);
	}

	@Override
	public Object parse(String text) throws NoMatchException, ParsingException
	{
		return doParse(text, null);
	}

	@Override
	public <T> T parse(String text, Class<T> targetClass) throws NoMatchException, ParsingException
	{
		Objects.requireNonNull(targetClass, "targetClass");
		try
		{
			return targetClass.cast(doParse(text, targetClass));
		}
		catch (ClassCastException e)
		{
			throw new ParsingException(text, targetClass, e);
		}
	}

	private Object doParse(String text, Class<?> targetClass) throws NoMatchException, ParsingException
	{
		Parsings.requireTextNotBlank(text, targetClass);
		for (Parser<?> p : (targetClass == null ? parsers.values() : parsers.get(targetClass)))
		{
			try
			{
				return p.parse(text);
			}
			catch (NoMatchException e)
			{
				// this parser could no match
				// ignore and move on to the next
				continue;
			}
		}

		// build Exception message
		StringBuilder msg = new StringBuilder();
		msg.append("No parser ");
		if (targetClass != null)
		{
			msg.append("with ");
			msg.append("entity type");
			msg.append(targetClass);
			msg.append(' ');
		}
		msg.append("could parse the text");

		throw new NoMatchException(text, targetClass, msg.toString());
	}
}