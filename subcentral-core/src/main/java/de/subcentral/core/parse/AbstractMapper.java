package de.subcentral.core.parse;

import java.util.Map;
import java.util.Objects;

import de.subcentral.core.util.SimplePropDescriptor;

public abstract class AbstractMapper<T> implements Mapper<T>
{
	protected final ParsePropService parsePropService;

	public AbstractMapper()
	{
		this(ParsingDefaults.getDefaultPropFromStringService());
	}

	public AbstractMapper(ParsePropService parsePropService)
	{
		this.parsePropService = Objects.requireNonNull(parsePropService, "parsePropService");
	}

	public ParsePropService getPropFromStringService()
	{
		return parsePropService;
	}

	@Override
	public T map(Map<SimplePropDescriptor, String> props) throws MappingException
	{
		try
		{
			return doMap(props);
		}
		catch (RuntimeException e)
		{
			throw new MappingException(props, getTargetType(), "Exception while mapping", e);
		}
	}

	protected abstract T doMap(Map<SimplePropDescriptor, String> props);

	protected abstract Class<?> getTargetType();
}
