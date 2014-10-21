package de.subcentral.core.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.subcentral.core.util.SimplePropDescriptor;

public abstract class AbstractMappingParser<T> implements Parser<T>
{
	protected final String									domain;
	protected List<MappingMatcher<SimplePropDescriptor>>	matchers			= new ArrayList<>();
	protected PropParsingService							propParsingService	= PropParsingService.DEFAULT;

	protected AbstractMappingParser(String domain)
	{
		this.domain = Objects.requireNonNull(domain);
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
		this.matchers = Objects.requireNonNull(matchers);
	}

	public PropParsingService getPropParsingService()
	{
		return propParsingService;
	}

	public void setPropParsingService(PropParsingService propParsingService)
	{
		this.propParsingService = Objects.requireNonNull(propParsingService);
	}

	@Override
	public T parse(String text) throws NoMatchException, ParsingException
	{
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
