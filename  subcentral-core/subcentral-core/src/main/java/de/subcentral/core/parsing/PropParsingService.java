package de.subcentral.core.parsing;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
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
import de.subcentral.core.util.TimeUtil;

public class PropParsingService
{
	/**
	 * The default item splitter. Splits the string into words (pattern {@code "[^\\w-]+"}). Is used by PropParsingService instances if no specific
	 * item splitter is defined. Common to all instances to save memory.
	 */
	public static Splitter									DEFAULT_ITEM_SPLITTER				= Splitter.onPattern("[^\\w-]+");

	/**
	 * The default map of fromString() functions. If a PropParsingService defines no specific fromString() function for a property or its type, the
	 * default map is searched. Common to all instances to save memory.
	 */
	public static final Map<Class<?>, Function<String, ?>>	DEFAULT_TYPE_FROM_STRING_FUNCTIONS	= new HashMap<>(16);
	static
	{
		// add the default type fromString functions
		// Boolean
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(Boolean.class, Boolean::parseBoolean);
		// Numbers
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(Integer.class, Integer::parseInt);
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(Long.class, Long::parseLong);
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(Float.class, Float::parseFloat);
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(Double.class, Double::parseDouble);
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(BigInteger.class, s -> new BigInteger(s));
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(BigDecimal.class, s -> new BigDecimal(s));
		// Temporals
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(Year.class, Year::parse);
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(YearMonth.class, YearMonth::parse);
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(LocalDate.class, LocalDate::parse);
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(LocalDateTime.class, LocalDateTime::parse);
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(ZonedDateTime.class, ZonedDateTime::parse);
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(Temporal.class, TimeUtil::parseTemporal);
		// Model specific types
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(Tag.class, s -> new Tag(s));
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(Group.class, s -> new Group(s));
		DEFAULT_TYPE_FROM_STRING_FUNCTIONS.put(Nuke.class, s -> new Nuke(s));
	}

	private Splitter										itemSplitter						= null;
	private Map<SimplePropDescriptor, Splitter>				propItemSplitter					= new HashMap<>(0);
	private Map<Class<?>, Function<String, ?>>				typeFromStringFunctions				= new HashMap<>(0);
	private Map<SimplePropDescriptor, Function<String, ?>>	propFromStringFunctions				= new HashMap<>(0);

	public PropParsingService()
	{

	}

	/**
	 * The defined item splitter. If null, {@link #DEFAULT_ITEM_SPLITTER} is used.
	 * 
	 * @return the item splitter
	 */
	public Splitter getItemSplitter()
	{
		return itemSplitter;
	}

	public void setItemSplitter(Splitter itemSplitter)
	{
		this.itemSplitter = itemSplitter;
	}

	public Map<SimplePropDescriptor, Splitter> getPropItemSplitter()
	{
		return propItemSplitter;
	}

	public void setPropItemSplitter(Map<SimplePropDescriptor, Splitter> propItemSplitter)
	{
		this.propItemSplitter = propItemSplitter;
	}

	public Map<Class<?>, Function<String, ?>> getTypeFromStringFunctions()
	{
		return typeFromStringFunctions;
	}

	public void setTypeFromStringFunctions(Map<Class<?>, Function<String, ?>> typeFromStringFunctions)
	{
		this.typeFromStringFunctions = typeFromStringFunctions;
	}

	public Map<SimplePropDescriptor, Function<String, ?>> getPropFromStringFunctions()
	{
		return propFromStringFunctions;
	}

	public void setPropFromStringFunctions(Map<SimplePropDescriptor, Function<String, ?>> propFromStringFunctions)
	{
		this.propFromStringFunctions = propFromStringFunctions;
	}

	public <P> P parse(Map<SimplePropDescriptor, String> info, SimplePropDescriptor propDescriptor, Class<P> propClass) throws ParsingException
	{
		String prop = info.get(propDescriptor);
		if (StringUtils.isBlank(prop))
		{
			return null;
		}
		return parse(prop, propDescriptor, propClass);
	}

	public <P> List<P> parseList(Map<SimplePropDescriptor, String> info, SimplePropDescriptor propDescriptor, Class<P> itemClass)
			throws ParsingException
	{
		String propListString = info.get(propDescriptor);
		if (StringUtils.isBlank(propListString))
		{
			return ImmutableList.of();
		}
		return parseList(propListString, propDescriptor, itemClass);
	}

	public <P> List<P> parseList(String propListString, SimplePropDescriptor propDescriptor, Class<P> itemClass) throws ParsingException
	{
		if (StringUtils.isBlank(propListString))
		{
			return ImmutableList.of();
		}
		Splitter splitter = propItemSplitter.getOrDefault(propDescriptor, itemSplitter);
		if (splitter == null)
		{
			splitter = DEFAULT_ITEM_SPLITTER;
		}
		ImmutableList.Builder<P> builder = ImmutableList.builder();
		Iterable<String> splitted = splitter.split(propListString);
		for (String propString : splitted)
		{
			builder.add(parse(propString, propDescriptor, itemClass));
		}
		return builder.build();
	}

	public <P> P parse(String propString, SimplePropDescriptor propDescriptor, Class<P> propClass) throws ParsingException
	{
		if (propString == null)
		{
			return null;
		}
		try
		{
			Function<String, ?> fn = propFromStringFunctions.get(propDescriptor);
			if (fn != null)
			{
				return propClass.cast(fn.apply(propString));
			}
			fn = typeFromStringFunctions.get(propClass);
			if (fn != null)
			{
				return propClass.cast(fn.apply(propString));
			}
			fn = DEFAULT_TYPE_FROM_STRING_FUNCTIONS.get(propClass);
			if (fn != null)
			{
				return propClass.cast(fn.apply(propString));
			}
			if (propClass.equals(String.class))
			{
				return propClass.cast(propString);
			}
			throw new ParsingException("Could not parse property string '" + propString + "' to property " + propDescriptor + " of " + propClass
					+ " (no appropriate fromString function registered)");
		}
		catch (Exception e)
		{
			throw new ParsingException("Could not parse property string '" + propString + "' to property " + propDescriptor + " of " + propClass, e);
		}
	}
}
