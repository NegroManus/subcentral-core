package de.subcentral.core.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import de.subcentral.core.util.SimplePropDescriptor;

public abstract class AbstractMappingParser<T> implements Parser<T>
{
	protected List<MappingMatcher<SimplePropDescriptor>>	matchers				= new ArrayList<>();
	protected PropFromStringService							propFromStringService	= SimplePropFromStringService.DEFAULT;

	public List<MappingMatcher<SimplePropDescriptor>> getMatchers()
	{
		return matchers;
	}

	public void setMatchers(List<MappingMatcher<SimplePropDescriptor>> matchers)
	{
		this.matchers = Objects.requireNonNull(matchers, "matchers");
	}

	public PropFromStringService getPropFromStringService()
	{
		return propFromStringService;
	}

	public void setPropFromStringService(SimplePropFromStringService propFromStringService)
	{
		this.propFromStringService = Objects.requireNonNull(propFromStringService, "propFromStringService");
	}

	@Override
	public T parse(String text) throws ParsingException
	{
		if (StringUtils.isBlank(text))
		{
			return null;
		}
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
