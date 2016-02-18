package de.subcentral.core.name;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import de.subcentral.core.util.SimplePropDescriptor;

public class SimplePropToStringService implements PropToStringService
{
	private final Map<Class<?>, Function<?, String>>				typeToStringFns	= new HashMap<>();
	private final Map<SimplePropDescriptor, Function<?, String>>	propToStringFns	= new HashMap<>();

	public Map<Class<?>, Function<?, String>> getTypeToStringFns()
	{
		return typeToStringFns;
	}

	public Map<SimplePropDescriptor, Function<?, String>> getPropToStringFns()
	{
		return propToStringFns;
	}

	@Override
	public String convert(SimplePropDescriptor propDescriptor, Object propValue)
	{
		try
		{
			return doConversion(propDescriptor, propValue);
		}
		catch (RuntimeException e)
		{
			throw new NamingException(propValue, "Exception while converting property " + propDescriptor + " to string", e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> String doConversion(SimplePropDescriptor propDescriptor, T propValue) throws ClassCastException
	{
		if (propValue == null)
		{
			return "";
		}
		// Search for function registered for property
		Function<?, String> toStringFn = propToStringFns.get(propDescriptor);
		if (toStringFn != null)
		{
			return ((Function<T, String>) toStringFn).apply(propValue);
		}
		// Search for function registered for type
		toStringFn = typeToStringFns.get(propValue.getClass());
		if (toStringFn != null)
		{
			return ((Function<T, String>) toStringFn).apply(propValue);
		}
		// Search for function registered for super type
		for (Map.Entry<Class<?>, Function<?, String>> entry : typeToStringFns.entrySet())
		{
			if (entry.getKey().isAssignableFrom(propValue.getClass()))
			{
				return ((Function<T, String>) entry.getValue()).apply(propValue);
			}
		}
		return propValue.toString();
	}
}
