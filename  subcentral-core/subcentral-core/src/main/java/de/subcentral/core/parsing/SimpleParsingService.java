package de.subcentral.core.parsing;

import java.util.List;

public class SimpleParsingService implements ParsingService
{
	private List<Parser<?>>	parsers;

	public List<Parser<?>> getParsers()
	{
		return parsers;
	}

	public void setParsers(List<Parser<?>> parsers)
	{
		this.parsers = parsers;
	}

	@Override
	public Object parse(String name, String domain)
	{
		for (Parser<?> p : parsers)
		{
			if (domain == null || domain.equals(p.getDomain()))
			{
				try
				{
					return p.parse(name);
				}
				catch (ParsingException e)
				{
					// this parser failed
					// ignore and move on to the next
				}
			}
		}
		throw new ParsingException("No parser " + (domain == null ? "" : "with domain '" + domain + "'") + " could parse input string '" + name + "'");
	}

	@Override
	public <T> T parseTyped(String name, String domain, Class<T> targetType)
	{
		for (Parser<?> p : parsers)
		{
			if ((domain == null || domain.equals(p.getDomain())) && (targetType == null || targetType.equals(p.getTargetType())))
			{
				try
				{
					return targetType.cast(p.parse(name));
				}
				catch (ParsingException e)
				{
					// this parser failed
					// ignore and move on to the next
				}
			}
		}
		throw new ParsingException("No parser with " + (domain == null ? "" : "domain '" + domain + "' and ") + "target type " + targetType.getName()
				+ " could parse input string '" + name + "'");
	}
}
