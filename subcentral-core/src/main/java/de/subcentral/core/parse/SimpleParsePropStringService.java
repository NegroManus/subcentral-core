package de.subcentral.core.parse;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Nuke;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.util.SimplePropDescriptor;
import de.subcentral.core.util.TimeUtil;

public class SimpleParsePropStringService implements ParsePropService
{
	private static final Logger								log									= LogManager.getLogger(SimpleParsePropStringService.class);

	/**
	 * The default element splitter. Used for splitting a list property string into its elements. Splits the string into words of alpha-num and '-' chars. Is used by PropParsingService instances if no
	 * specific item splitter is defined. Common to all instances to save memory.
	 */
	public static final Splitter							DEFAULT_ELEMENT_SPLITTER			= Splitter.on(CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.is('-')).negate()).omitEmptyStrings();

	/**
	 * The default map of fromString() functions. If a PropParsingService defines no specific fromString() function for a property or its type, the default map is searched. Common to all instances to
	 * save memory.
	 */
	private static final Map<Class<?>, Function<String, ?>>	DEFAULT_TYPE_FROM_STRING_FUNCTIONS	= initDefaultTypeFromStringFunctions();

	private Splitter										elementSplitter						= null;
	private Map<SimplePropDescriptor, Splitter>				propElementSplitters				= new HashMap<>(0);
	private Map<Class<?>, Function<String, ?>>				typeFromStringFunctions				= new HashMap<>(0);
	private Map<SimplePropDescriptor, Function<String, ?>>	propFromStringFunctions				= new HashMap<>(0);

	private static final Map<Class<?>, Function<String, ?>> initDefaultTypeFromStringFunctions()
	{
		ImmutableMap.Builder<Class<?>, Function<String, ?>> typeFns = ImmutableMap.builder();
		// add the default type fromString functions
		// Boolean
		typeFns.put(boolean.class, Boolean::parseBoolean);
		typeFns.put(Boolean.class, Boolean::valueOf);
		// Numbers
		typeFns.put(int.class, Integer::parseInt);
		typeFns.put(long.class, Long::parseLong);
		typeFns.put(float.class, Float::parseFloat);
		typeFns.put(double.class, Double::parseDouble);
		typeFns.put(Integer.class, Integer::valueOf);
		typeFns.put(Long.class, Long::valueOf);
		typeFns.put(Float.class, Float::valueOf);
		typeFns.put(Double.class, Double::valueOf);
		typeFns.put(BigInteger.class, BigInteger::new);
		typeFns.put(BigDecimal.class, BigDecimal::new);
		// Temporals
		typeFns.put(Year.class, Year::parse);
		typeFns.put(YearMonth.class, YearMonth::parse);
		typeFns.put(LocalDate.class, LocalDate::parse);
		typeFns.put(LocalDateTime.class, LocalDateTime::parse);
		typeFns.put(ZonedDateTime.class, ZonedDateTime::parse);
		typeFns.put(Temporal.class, TimeUtil::parseTemporal);
		// Model specific types
		typeFns.put(Tag.class, Tag::new);
		typeFns.put(Group.class, Group::new);
		typeFns.put(Nuke.class, Nuke::new);

		return typeFns.build();
	}

	/**
	 * The defined item splitter. If null, {@link #DEFAULT_ELEMENT_SPLITTER} is used.
	 * 
	 * @return the item splitter
	 */
	public Splitter getElementSplitter()
	{
		return elementSplitter;
	}

	public void setElementSplitter(Splitter elementSplitter)
	{
		this.elementSplitter = elementSplitter;
	}

	public Map<SimplePropDescriptor, Splitter> getPropElementSplitters()
	{
		return propElementSplitters;
	}

	public void setPropElementSplitters(Map<SimplePropDescriptor, Splitter> propElementSplitters)
	{
		this.propElementSplitters = propElementSplitters;
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

	@Override
	public <P> List<P> parseList(String propListString, SimplePropDescriptor propDescriptor, Class<P> elementClass) throws ParsingException
	{
		if (propListString == null)
		{
			return ImmutableList.of();
		}
		Splitter splitter = propElementSplitters.getOrDefault(propDescriptor, elementSplitter);
		if (splitter == null)
		{
			splitter = DEFAULT_ELEMENT_SPLITTER;
		}
		ImmutableList.Builder<P> builder = ImmutableList.builder();
		Iterable<String> splitted = splitter.split(propListString);
		for (String elementString : splitted)
		{
			P parsedElem = parse(elementString, propDescriptor, elementClass);
			if (parsedElem != null)
			{
				builder.add(parsedElem);
			}
			else
			{
				log.warn("Parsed an element of a list property to null, ignoring this element. propListString={}, elementString={}, propDescriptor={}, elementClass={}",
						propListString,
						elementString,
						propDescriptor,
						elementClass);
			}
		}
		return builder.build();
	}

	@Override
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
			throw new ParsingException(propString, propClass, "Could not parse property " + propDescriptor + ": no appropriate fromString function registered");
		}
		catch (Exception e)
		{
			throw new ParsingException(propString, propClass, "Could not parse property " + propDescriptor, e);
		}
	}
}
