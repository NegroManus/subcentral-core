package de.subcentral.core.naming;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.util.Separation;
import de.subcentral.core.util.SimplePropDescriptor;

public class PropSequenceNameBuilder
{
	private final PropToStringService		propToStringService;
	private final Set<Separation>			separations;
	private final Function<String, String>	finalFormatter;
	private final StringBuilder				sb	= new StringBuilder();
	private SimplePropDescriptor			lastProp;

	public PropSequenceNameBuilder(PropToStringService propToStringService, Set<Separation> separations, Function<String, String> finalFormatter)
	{
		this.propToStringService = Objects.requireNonNull(propToStringService, "propToStringService");
		this.separations = Objects.requireNonNull(separations, "separations");
		this.finalFormatter = finalFormatter;
	}

	public void overwriteLastProperty(SimplePropDescriptor lastProp)
	{
		this.lastProp = lastProp;
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

	public PropSequenceNameBuilder appendIfElse(SimplePropDescriptor propDescriptor, Object ifValue, Object elseValue, boolean condition)
	{
		if (condition)
		{
			return append(propDescriptor, ifValue, null);
		}
		return append(propDescriptor, elseValue, null);
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
			sb.append(Separation.getSeparatorBetween(lastProp, propDescr, separationType, separations));
		}
		sb.append(propValue);
		lastProp = propDescr;
		return this;
	}

	@Override
	public String toString()
	{
		if (finalFormatter == null)
		{
			return sb.toString();
		}
		return finalFormatter.apply(sb.toString());
	}
}
