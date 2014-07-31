package de.subcentral.core.naming;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.util.SeparationDefinition;
import de.subcentral.core.util.SimplePropDescriptor;

public class NameBuilder
{
	// final properties
	private final ImmutableMap<SimplePropDescriptor, Function<?, String>>	propertyToStringFunctions;
	private final ImmutableSet<SeparationDefinition>						separators;
	private final UnaryOperator<String>										wholeNameOperator;

	private final StringBuilder												sb;
	private SimplePropDescriptor											lastProp	= null;

	protected NameBuilder(NameBuilderFactory factory)
	{
		propertyToStringFunctions = ImmutableMap.copyOf(factory.getPropertyToStringFunctions());
		separators = ImmutableSet.copyOf(factory.getSeparators());
		wholeNameOperator = factory.getWholeNameOperator();
		sb = new StringBuilder();
	}

	public NameBuilder appendAllIfNotEmpty(SimplePropDescriptor simplePropDescriptor, Collection<?> propCollection)
	{
		if (!propCollection.isEmpty())
		{
			appendAll(simplePropDescriptor, propCollection);
		}
		return this;
	}

	public NameBuilder appendAll(SimplePropDescriptor simplePropDescriptor, Iterable<?> propIterable)
	{
		propIterable.forEach(p -> append(simplePropDescriptor, p));
		return this;
	}

	public NameBuilder appendIfNotNull(SimplePropDescriptor simplePropDescriptor, Object propValue)
	{
		return appendIf(simplePropDescriptor, propValue, propValue != null);
	}

	public NameBuilder appendIf(SimplePropDescriptor simplePropDescriptor, Object propValue, boolean condition)
	{
		if (condition)
		{
			return append(simplePropDescriptor, propValue, null);
		}
		return this;
	}

	public NameBuilder append(SimplePropDescriptor simplePropDescriptor, Object propValue)
	{
		return append(simplePropDescriptor, propValue, null);
	}

	public NameBuilder append(SimplePropDescriptor simplePropDescriptor, Object propValue, String separationType)
	{
		return appendString(simplePropDescriptor, propToString(simplePropDescriptor, propValue), separationType);
	}

	public NameBuilder appendString(SimplePropDescriptor simplePropDescriptor, String propValue)
	{
		return appendString(simplePropDescriptor, propValue, null);
	}

	public NameBuilder appendString(SimplePropDescriptor simplePropDescriptor, String propValue, String separationType)
	{
		if (lastProp != null)
		{
			sb.append(SeparationDefinition.getSeparatorBetween(lastProp, simplePropDescriptor, separationType, separators));
		}
		sb.append(propValue);
		lastProp = simplePropDescriptor;
		return this;
	}

	public String build()
	{
		return wholeNameOperator.apply(sb.toString());
	}

	@Override
	public String toString()
	{
		return build();
	}

	/**
	 * 
	 * @param propDescriptor
	 * @param propValue
	 * @return
	 * @throws ClassCastException
	 *             If the actual input type of the registered Function cannot be casted to the property type.
	 */
	private <P> String propToString(SimplePropDescriptor propDescriptor, P propValue) throws ClassCastException
	{
		if (propValue == null)
		{
			return "";
		}
		@SuppressWarnings("unchecked")
		Function<P, String> f = (Function<P, String>) propertyToStringFunctions.get(propDescriptor);
		if (f == null)
		{
			return propValue.toString();
		}
		return f.apply(propValue);
	}
}
