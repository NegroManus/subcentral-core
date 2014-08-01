package de.subcentral.core.parsing;

import java.util.List;
import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public abstract class AbstractPropertyParser<T> implements Parser<T>
{
	protected final String										domain;
	protected final List<MappingMatcher<SimplePropDescriptor>>	matchers;
	protected final PropParsingService							pps;

	protected AbstractPropertyParser(String domain, List<MappingMatcher<SimplePropDescriptor>> matchers, PropParsingService pps)
	{
		this.domain = domain;
		this.matchers = matchers;
		this.pps = pps;
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	@Override
	public T parse(String s) throws ParsingException
	{
		for (MappingMatcher<SimplePropDescriptor> matcher : matchers)
		{
			Map<SimplePropDescriptor, String> matchResult = matcher.match(s);
			if (matchResult != null)
			{
				return map(matchResult);
			}
		}
		throw new ParsingException("Now matcher could parse input string '" + s + "'");
	}

	protected abstract T map(Map<SimplePropDescriptor, String> props);
}
