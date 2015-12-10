package de.subcentral.core.parsing;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.util.SimplePropDescriptor;

public abstract class AbstractMappingParser<T> implements Parser<T>
{
	protected final List<MappingMatcher<SimplePropDescriptor>> matchers;

	public AbstractMappingParser(Iterable<MappingMatcher<SimplePropDescriptor>> matchers)
	{
		this.matchers = ImmutableList.copyOf(matchers);
	}

	public List<MappingMatcher<SimplePropDescriptor>> getMatchers()
	{
		return matchers;
	}

	@Override
	public T parse(String text) throws ParsingException
	{
		try
		{
			for (MappingMatcher<SimplePropDescriptor> matcher : matchers)
			{
				Map<SimplePropDescriptor, String> matchResult = matcher.match(text);
				if (!matchResult.isEmpty())
				{
					return map(matchResult);
				}
			}
		}
		catch (RuntimeException e)
		{
			throw new ParsingException(text, "Exception while parsing", e);
		}
		return null;
	}

	protected abstract T map(Map<SimplePropDescriptor, String> props);
}
