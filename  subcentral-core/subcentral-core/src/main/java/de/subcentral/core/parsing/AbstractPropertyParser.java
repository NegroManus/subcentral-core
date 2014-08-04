package de.subcentral.core.parsing;

import java.util.List;
import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public abstract class AbstractPropertyParser<T> implements Parser<T>
{
	protected final String									domain;
	protected List<MappingMatcher<SimplePropDescriptor>>	matchers;
	protected PropParsingService							pps;

	protected AbstractPropertyParser(String domain)
	{
		this.domain = domain;
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	public List<MappingMatcher<SimplePropDescriptor>> getMatchers()
	{
		return matchers;
	}

	public void setMatchers(List<MappingMatcher<SimplePropDescriptor>> matchers)
	{
		this.matchers = matchers;
	}

	public PropParsingService getPps()
	{
		return pps;
	}

	public void setPps(PropParsingService pps)
	{
		this.pps = pps;
	}

	@Override
	public T parse(String name) throws NoMatchException, ParsingException
	{
		try
		{
			for (MappingMatcher<SimplePropDescriptor> matcher : matchers)
			{
				Map<SimplePropDescriptor, String> matchResult = matcher.match(name);
				if (matchResult != null)
				{
					return map(matchResult);
				}
			}
		}
		catch (Exception e)
		{
			throw new ParsingException("Could not parse input string '" + name + "'", e);
		}
		throw new NoMatchException("No matcher could parse the name '" + name + "'");
	}

	protected abstract T map(Map<SimplePropDescriptor, String> props);
}
