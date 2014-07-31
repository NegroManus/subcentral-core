package de.subcentral.core.naming;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.jsoup.helper.Validate;

import de.subcentral.core.util.SeparationDefinition;
import de.subcentral.core.util.SimplePropDescriptor;

public abstract class AbstractPropertySequenceNamer<T> implements Namer<T>
{
	private Map<SimplePropDescriptor, Function<?, String>>	propertyToStringFunctions	= new HashMap<>();
	private Set<SeparationDefinition>						separators					= new HashSet<>();
	private UnaryOperator<String>							wholeNameOperator			= UnaryOperator.identity();

	public Map<SimplePropDescriptor, Function<?, String>> getPropertyToStringFunctions()
	{
		return propertyToStringFunctions;
	}

	/**
	 * 
	 * @param propertyToStringFunctions
	 *            The toString() functions for the properties. A Map of {property name -> toString() function}.
	 */
	public void setPropertyToStringFunctions(Map<SimplePropDescriptor, Function<?, String>> propertyToStringFunctions)
	{
		Validate.notNull(propertyToStringFunctions);
		this.propertyToStringFunctions = propertyToStringFunctions;
	}

	public Set<SeparationDefinition> getSeparators()
	{
		return separators;
	}

	/**
	 * 
	 * @param separators
	 *            The separators between the properties.
	 */
	public void setSeparators(Set<SeparationDefinition> separators)
	{
		Validate.notNull(separators);
		this.separators = separators;
	}

	public UnaryOperator<String> getWholeNameOperator()
	{
		return wholeNameOperator;
	}

	/**
	 * 
	 * @param wholeNameOperator
	 *            The operator which operates on the whole name after it was constructed of the properties and separators.
	 * 
	 */
	public void setWholeNameOperator(UnaryOperator<String> wholeNameOperator)
	{
		Validate.notNull(wholeNameOperator);
		this.wholeNameOperator = wholeNameOperator;
	}

	@Override
	public String name(T candidate, NamingService namingService, Map<String, Object> parameters) throws NamingException
	{
		if (candidate == null)
		{
			return null;
		}
		try
		{
			return wholeNameOperator.apply(doName(candidate, namingService, parameters));
		}
		catch (Exception e)
		{
			throw new NamingException(candidate, e);
		}
	}

	/**
	 * 
	 * @param candidate
	 *            The candidate. Never null.
	 * @param namingService
	 *            The NamingService. May null.
	 * @param parameters
	 *            The parameters. Not null, may empty.
	 * @return The name of the candidate. Will be processed by {@link #formatWholeName(String)}.
	 * @throws Exception
	 *             Whatever exception occurs while naming the candidate. Will be wrapped into a NamingException and thrown.
	 */
	protected abstract String doName(T candidate, NamingService namingService, Map<String, Object> parameters) throws Exception;

	protected String getSeparatorBetween(SimplePropDescriptor firstProperty, SimplePropDescriptor secondProperty, String separationType)
	{
		return SeparationDefinition.getSeparatorBetween(firstProperty, secondProperty, separationType, separators);
	}

	protected <P> String propToString(SimplePropDescriptor propDescriptor, P propValue) throws ClassCastException
	{
		return propToString(propDescriptor, propValue, null);
	}

	/**
	 * 
	 * @param propDescriptor
	 * @param propValue
	 * @return
	 * @throws ClassCastException
	 *             If the actual input type of the registered Function cannot be casted to the property type.
	 */
	protected <P> String propToString(SimplePropDescriptor propDescriptor, P propValue, String separationType) throws ClassCastException
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

	protected class Builder
	{
		private final StringBuilder		sb;
		private SimplePropDescriptor	lastProperty	= null;

		protected Builder()
		{
			sb = new StringBuilder();
		}

		protected Builder appendAllIfNotEmpty(SimplePropDescriptor SimplePropertyDescriptor, Collection<?> propertyCollection)
		{
			if (!propertyCollection.isEmpty())
			{
				appendAll(SimplePropertyDescriptor, propertyCollection);
			}
			return this;
		}

		protected Builder appendAll(SimplePropDescriptor SimplePropertyDescriptor, Iterable<?> propertyIterable)
		{
			propertyIterable.forEach(p -> append(SimplePropertyDescriptor, p));
			return this;
		}

		protected Builder appendIfNotNull(SimplePropDescriptor SimplePropertyDescriptor, Object propertyValue)
		{
			return appendIf(SimplePropertyDescriptor, propertyValue, propertyValue != null);
		}

		protected Builder appendIf(SimplePropDescriptor SimplePropertyDescriptor, Object propertyValue, boolean condition)
		{
			if (condition)
			{
				return append(SimplePropertyDescriptor, propertyValue, null);
			}
			return this;
		}

		protected Builder append(SimplePropDescriptor SimplePropertyDescriptor, Object propertyValue)
		{
			return append(SimplePropertyDescriptor, propertyValue, null);
		}

		protected Builder append(SimplePropDescriptor SimplePropertyDescriptor, Object propertyValue, String separationType)
		{
			return appendString(SimplePropertyDescriptor, propToString(SimplePropertyDescriptor, propertyValue, separationType), separationType);
		}

		protected Builder appendString(SimplePropDescriptor simplePropDescriptor, String propertyValue)
		{
			return appendString(simplePropDescriptor, propertyValue, null);
		}

		protected Builder appendString(SimplePropDescriptor SimplePropertyDescriptor, String propertyValue, String separationType)
		{
			if (lastProperty != null)
			{
				sb.append(SeparationDefinition.getSeparatorBetween(lastProperty, SimplePropertyDescriptor, separationType, separators));
			}
			sb.append(propertyValue);
			lastProperty = SimplePropertyDescriptor;
			return this;
		}

		protected String build()
		{
			return sb.toString();
		}

		@Override
		public String toString()
		{
			return sb.toString();
		}
	}
}
