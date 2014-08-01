package de.subcentral.core.parsing;

import java.util.List;

public class ParsingServiceImpl implements ParsingService
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
		throw new ParsingException("No parser in domain " + domain + " could parse input string '" + name + "'");
	}

	@Override
	public <T> T parse(String name, String domain, Class<T> targetClass)
	{
		for (Parser<?> p : parsers)
		{
			if ((domain == null || domain.equals(p.getDomain())) && (targetClass == null || targetClass.equals(p.getTargetClass())))
			{
				try
				{
					return targetClass.cast(p.parse(name));
				}
				catch (ParsingException e)
				{
					// this parser failed
					// ignore and move on to the next
				}
			}
		}
		throw new ParsingException("No parser in domain " + domain + " could parse input string '" + name + "'");
	}
}
