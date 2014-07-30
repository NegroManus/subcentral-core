package de.subcentral.core.parsing;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Nuke;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.util.SimplePropDescriptor;

public class PropParsingService
{
	public static final Splitter							DEFAULT_ITEM_SPLITTER		= Splitter.onPattern("[^\\w-]+");

	private Splitter										itemSplitter				= DEFAULT_ITEM_SPLITTER;
	private Map<Class<?>, Function<String, ?>>				typeFromStringFunctions		= new HashMap<>();
	private Map<SimplePropDescriptor, Function<String, ?>>	propertyFromStringFunctions	= new HashMap<>();

	public PropParsingService()
	{
		// add the default type fromString functions
		// Boolean
		typeFromStringFunctions.put(Boolean.class, Boolean::parseBoolean);
		// Numbers
		typeFromStringFunctions.put(Integer.class, Integer::parseInt);
		typeFromStringFunctions.put(Long.class, Long::parseLong);
		typeFromStringFunctions.put(Float.class, Float::parseFloat);
		typeFromStringFunctions.put(Double.class, Double::parseDouble);
		typeFromStringFunctions.put(BigInteger.class, s -> new BigInteger(s));
		typeFromStringFunctions.put(BigDecimal.class, s -> new BigDecimal(s));
		// Temporals
		typeFromStringFunctions.put(Year.class, Year::parse);
		typeFromStringFunctions.put(LocalDate.class, LocalDate::parse);
		typeFromStringFunctions.put(LocalDateTime.class, LocalDateTime::parse);
		typeFromStringFunctions.put(ZonedDateTime.class, ZonedDateTime::parse);
		// Model specific types
		typeFromStringFunctions.put(Tag.class, s -> new Tag(s));
		typeFromStringFunctions.put(Group.class, s -> new Group(s));
		typeFromStringFunctions.put(Nuke.class, s -> new Nuke(s));
	}

	public Splitter getItemSplitter()
	{
		return itemSplitter;
	}

	public void setItemSplitter(Splitter itemSplitter)
	{
		this.itemSplitter = itemSplitter;
	}

	public Map<Class<?>, Function<String, ?>> getTypeFromStringFunctions()
	{
		return typeFromStringFunctions;
	}

	public void setTypeFromStringFunctions(Map<Class<?>, Function<String, ?>> typeFromStringFunctions)
	{
		this.typeFromStringFunctions = typeFromStringFunctions;
	}

	public Map<SimplePropDescriptor, Function<String, ?>> getPropertyFromStringFunctions()
	{
		return propertyFromStringFunctions;
	}

	public void setPropertyFromStringFunctions(Map<SimplePropDescriptor, Function<String, ?>> propertyFromStringFunctions)
	{
		this.propertyFromStringFunctions = propertyFromStringFunctions;
	}

	public <P> P parseProp(Map<SimplePropDescriptor, String> info, SimplePropDescriptor propDescriptor, Class<P> propClass)
	{
		String prop = info.get(propDescriptor);
		if (StringUtils.isBlank(prop))
		{
			return null;
		}
		return doParseProp(prop, propDescriptor, propClass);
	}

	public <P> List<P> parsePropList(Map<SimplePropDescriptor, String> info, SimplePropDescriptor propDescriptor, Class<P> propClass)
	{
		String propList = info.get(propDescriptor);
		if (StringUtils.isBlank(propList))
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<P> builder = ImmutableList.builder();
		Iterable<String> splitted = itemSplitter.split(propList);
		for (String prop : splitted)
		{
			builder.add(doParseProp(prop, propDescriptor, propClass));
		}
		return builder.build();
	}

	private <P> P doParseProp(String prop, SimplePropDescriptor propDescriptor, Class<P> propClass)
	{
		Function<String, ?> propertyFunction = propertyFromStringFunctions.get(propDescriptor);
		if (propertyFunction != null)
		{
			return propClass.cast(propertyFunction.apply(prop));
		}
		Function<String, ?> typeFunction = typeFromStringFunctions.get(propClass);
		if (typeFunction != null)
		{
			return propClass.cast(typeFunction.apply(prop));
		}

		if (propClass.equals(String.class))
		{
			return propClass.cast(prop);
		}
		throw new ParsingException("Could not parse property string '" + prop + "' to property " + propDescriptor + " of " + propClass);
	}

}
