package de.subcentral.core.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.subcentral.core.util.SimplePropDescriptor;

public abstract class AbstractMappingParser<T> implements Parser<T>
{
	protected List<MappingMatcher<SimplePropDescriptor>>	matchers				= new ArrayList<>();
	protected SimplePropFromStringService					propFromStringService	= SimplePropFromStringService.DEFAULT;

	public List<MappingMatcher<SimplePropDescriptor>> getMatchers()
	{
		return matchers;
	}

	public void setMatchers(List<MappingMatcher<SimplePropDescriptor>> matchers)
	{
		this.matchers = Objects.requireNonNull(matchers);
	}

	public SimplePropFromStringService getPropFromStringService()
	{
		return propFromStringService;
	}

	public void setPropFromStringService(SimplePropFromStringService propFromStringService)
	{
		this.propFromStringService = Objects.requireNonNull(propFromStringService);
	}

	@Override
	public T parse(String text) throws NoMatchException, ParsingException
	{
		Parsings.requireNotBlank(text, null);
		try
		{
			for (MappingMatcher<SimplePropDescriptor> matcher : matchers)
			{
				Map<SimplePropDescriptor, String> matchResult = matcher.match(text);
				if (matchResult != null)
				{
					return map(matchResult);
				}
			}
		}
		catch (Exception e)
		{
			throw new ParsingException(text, null, "Exception while parsing", e);
		}
		throw new NoMatchException(text, null, "No matcher could match");
	}

	protected abstract T map(Map<SimplePropDescriptor, String> props);
}
