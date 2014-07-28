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

public abstract class GenericMapper<T> implements Mapper<T>
{
	public static final Splitter							DEFAULT_ITEM_SPLITTER		= Splitter.onPattern("[^\\w-]+");

	private Splitter										itemSplitter				= DEFAULT_ITEM_SPLITTER;
	private Map<Class<?>, Function<String, ?>>				typeFromStringFunctions		= new HashMap<>();
	private Map<SimplePropDescriptor, Function<String, ?>>	propertyFromStringFunctions	= new HashMap<>();

	public GenericMapper()
	{
		// add the default type fromString functions
		// Boolean
		Function<String, Boolean> boolFn = s -> Boolean.parseBoolean(s);

		// Numbers
		Function<String, Integer> intFn = s -> Integer.parseInt(s);
		Function<String, Long> longFn = s -> Long.parseLong(s);
		Function<String, Float> floatFn = s -> Float.parseFloat(s);
		Function<String, Double> doubleFn = s -> Double.parseDouble(s);
		Function<String, BigInteger> biFn = s -> new BigInteger(s);
		Function<String, BigDecimal> bdFn = s -> new BigDecimal(s);

		// Temporals
		Function<String, Year> yearFn = s -> Year.parse(s);
		Function<String, LocalDate> dateFn = s -> LocalDate.parse(s);
		Function<String, LocalDateTime> dateTimeFn = s -> LocalDateTime.parse(s);
		Function<String, ZonedDateTime> zonedDateTimeFn = s -> ZonedDateTime.parse(s);

		// Model specific types
		Function<String, Tag> tagFn = s -> new Tag(s);
		Function<String, Group> grpFn = s -> new Group(s);
		Function<String, Nuke> nukeFn = s -> new Nuke(s);

		typeFromStringFunctions.put(Boolean.class, boolFn);
		typeFromStringFunctions.put(Integer.class, intFn);
		typeFromStringFunctions.put(Long.class, longFn);
		typeFromStringFunctions.put(Float.class, floatFn);
		typeFromStringFunctions.put(Double.class, doubleFn);
		typeFromStringFunctions.put(BigInteger.class, biFn);
		typeFromStringFunctions.put(BigDecimal.class, bdFn);
		typeFromStringFunctions.put(Year.class, yearFn);
		typeFromStringFunctions.put(LocalDate.class, dateFn);
		typeFromStringFunctions.put(LocalDateTime.class, dateTimeFn);
		typeFromStringFunctions.put(ZonedDateTime.class, zonedDateTimeFn);
		typeFromStringFunctions.put(Tag.class, tagFn);
		typeFromStringFunctions.put(Group.class, grpFn);
		typeFromStringFunctions.put(Nuke.class, nukeFn);
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

	protected <P> P parseProp(Map<SimplePropDescriptor, String> info, SimplePropDescriptor propDescriptor, Class<P> propClass)
	{
		String prop = info.get(propDescriptor);
		if (StringUtils.isBlank(prop))
		{
			return null;
		}
		return doParseProp(prop, propDescriptor, propClass);
	}

	protected <P> List<P> parsePropList(Map<SimplePropDescriptor, String> info, SimplePropDescriptor propDescriptor, Class<P> propClass)
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
