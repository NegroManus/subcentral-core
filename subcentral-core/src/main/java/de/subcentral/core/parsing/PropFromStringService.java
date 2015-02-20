package de.subcentral.core.parsing;

import java.util.List;
import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public interface PropFromStringService
{
	public default <P> P parse(Map<SimplePropDescriptor, String> info, SimplePropDescriptor propDescriptor, Class<P> propClass)
			throws ParsingException
	{
		return parse(info.get(propDescriptor), propDescriptor, propClass);
	}

	public default <P> List<P> parseList(Map<SimplePropDescriptor, String> info, SimplePropDescriptor propDescriptor, Class<P> elementClass)
			throws ParsingException
	{
		return parseList(info.get(propDescriptor), propDescriptor, elementClass);
	}

	public <P> List<P> parseList(String propListString, SimplePropDescriptor propDescriptor, Class<P> elementClass) throws ParsingException;

	public <P> P parse(String propString, SimplePropDescriptor propDescriptor, Class<P> propClass) throws ParsingException;
}
