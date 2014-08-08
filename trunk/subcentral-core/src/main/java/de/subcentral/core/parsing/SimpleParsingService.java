package de.subcentral.core.parsing;

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
			throw new ParsingException(e);
		}
	}

	private Object doParse(String text, String domain, Class<?> entityType) throws NoMatchException
	{
		Parsings.requireTextNotBlank(text);
		for (Parser<?> p : entityType == null ? parsers.values() : parsers.get(entityType))
		{
			if (domain == null || domain.equals(p.getDomain()))
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
		msg.append("could parse the text '");
		msg.append(text);
		msg.append('\'');

		throw new NoMatchException(msg.toString());
	}
}
