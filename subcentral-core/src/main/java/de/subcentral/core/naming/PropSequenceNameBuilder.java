package de.subcentral.core.naming;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

import de.subcentral.core.util.SeparationDefinition;
import de.subcentral.core.util.SimplePropDescriptor;

public class PropSequenceNameBuilder
{
	private final PropToStringService		propToStringService;
	private final Set<SeparationDefinition>	separators;
	private final UnaryOperator<String>		wholeNameOperator;
	private final StringBuilder				sb	= new StringBuilder();
	private SimplePropDescriptor			lastProp;

	public PropSequenceNameBuilder(PropToStringService propToStringService, Set<SeparationDefinition> separators,
			UnaryOperator<String> wholeNameOperator)
	{
		this.propToStringService = Objects.requireNonNull(propToStringService, "propToStringService");
		this.separators = Objects.requireNonNull(separators, "separators");
		this.wholeNameOperator = wholeNameOperator;
	}

	public void overwriteLastProperty(SimplePropDescriptor lastProp)
	{
		this.lastProp = lastProp;
	}

	public PropSequenceNameBuilder appendAllIfNotEmpty(SimplePropDescriptor propDescriptor, Collection<?> propCollection)
	{
		if (!propCollection.isEmpty())
		{
			appendAll(propDescriptor, propCollection);
		}
		return this;
	}

	public PropSequenceNameBuilder appendAll(SimplePropDescriptor propDescriptor, Iterable<?> propertyIterable)
	{
		for (Object prop : propertyIterable)
		{
			append(propDescriptor, prop);
		}
		return this;
	}

	public PropSequenceNameBuilder appendIfNotNull(SimplePropDescriptor propDescriptor, Object propValue)
	{
		return appendIf(propDescriptor, propValue, propValue != null);
	}

	public PropSequenceNameBuilder appendIf(SimplePropDescriptor propDescriptor, Object propValue, boolean condition)
	{
		if (condition)
		{
			return append(propDescriptor, propValue, null);
		}
		return this;
	}

	public PropSequenceNameBuilder append(SimplePropDescriptor propDescriptor, Object propValue)
	{
		return append(propDescriptor, propValue, null);
	}

	public PropSequenceNameBuilder append(SimplePropDescriptor propDescriptor, Object propValue, String separationType)
	{
		return appendString(propDescriptor, propToStringService.convert(propDescriptor, propValue), separationType);
	}

	public PropSequenceNameBuilder appendString(SimplePropDescriptor propDescr, String propValue)
	{
		return appendString(propDescr, propValue, null);
	}

	public PropSequenceNameBuilder appendString(SimplePropDescriptor propDescr, String propValue, String separationType)
	{
		if (lastProp != null)
		{
			sb.append(SeparationDefinition.getSeparatorBetween(lastProp, propDescr, separationType, separators));
		}
		sb.append(propValue);
		lastProp = propDescr;
		return this;
	}

	@Override
	public String toString()
	{
		if (wholeNameOperator == null)
		{
			return sb.toString();
		}
		return wholeNameOperator.apply(sb.toString());
	}
}
