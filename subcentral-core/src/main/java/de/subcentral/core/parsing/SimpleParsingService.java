package de.subcentral.core.parsing;

import java.util.ArrayList;
import java.util.List;

public class SimpleParsingService implements ParsingService
{
	private List<Parser<?>>	parsers	= new ArrayList<>(0);

	public List<Parser<?>> getParsers()
	{
		return parsers;
	}

	public void setParsers(List<Parser<?>> parsers)
	{
		this.parsers = parsers;
	}

	@Override
	public Object parse(String text, String domain) throws NoMatchException, ParsingException
	{
		for (Parser<?> p : parsers)
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
		throw new NoMatchException("No parser " + (domain == null ? "" : "with domain '" + domain + "'") + " could parse the text '" + text + "'");
	}

	@Override
	public <T> T parseTyped(String text, String domain, Class<T> entityType) throws NoMatchException, ParsingException
	{
		for (Parser<?> p : parsers)
		{
			if ((domain == null || domain.equals(p.getDomain())) && (entityType == null || entityType.equals(p.getEntityType())))
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
			}
		}
		throw new NoMatchException("No parser with " + (domain == null ? "" : "domain '" + domain + "' and ") + "entity type " + entityType.getName()
				+ " could parse the text '" + text + "'");
	}
}
