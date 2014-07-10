package de.subcentral.core.naming;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.jsoup.helper.Validate;

import de.subcentral.core.util.SeparationDefinition;

public abstract class AbstractSeparatedPropertiesNamer<T> implements Namer<T>
{
	private Map<PropertyDescriptor, Function<?, String>>	propertyToStringFunctions	= new HashMap<>();
	private Set<SeparationDefinition>						separators					= new HashSet<>();
	private UnaryOperator<String>							wholeNameOperator			= UnaryOperator.identity();

	public Map<PropertyDescriptor, Function<?, String>> getPropertyToStringFunctions()
	{
		return propertyToStringFunctions;
	}

	/**
	 * 
	 * @param propertyToStringFunctions
	 *            The toString() functions for the properties. A Map of {property name -> toString() function}.
	 */
	public void setPropertyToStringFunctions(Map<PropertyDescriptor, Function<?, String>> propertyToStringFunctions)
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

	protected String getSeparatorBetween(PropertyDescriptor firstProperty, PropertyDescriptor secondProperty, String separationType)
	{
		return SeparationDefinition.getSeparatorBetween(firstProperty, secondProperty, separationType, separators);
	}

	protected <P> String propToString(PropertyDescriptor propDescriptor, P propValue) throws ClassCastException
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
	protected <P> String propToString(PropertyDescriptor propDescriptor, P propValue, String separationType) throws ClassCastException
	{
		if (propValue == null)
		{
			return null;
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
		private final StringBuilder	sb;
		private PropertyDescriptor	lastProperty	= null;

		protected Builder()
		{
			sb = new StringBuilder();
		}

		protected Builder appendIterable(PropertyDescriptor propertyDescriptor, Iterable<?> propertyIterable)
		{
			propertyIterable.forEach(p -> append(propertyDescriptor, p));
			return this;
		}

		protected Builder appendCollectionIfNotEmpty(PropertyDescriptor propertyDescriptor, Collection<?> propertyCollection)
		{
			if (!propertyCollection.isEmpty())
			{
				appendIterable(propertyDescriptor, propertyCollection);
			}
			return this;
		}

		protected Builder appendIfNotNull(PropertyDescriptor propertyDescriptor, Object propertyValue)
		{
			return appendIf(propertyDescriptor, propertyValue, propertyValue != null);
		}

		protected Builder appendIf(PropertyDescriptor propertyDescriptor, Object propertyValue, boolean condition)
		{
			if (condition)
			{
				return append(propertyDescriptor, propertyValue, null);
			}
			return this;
		}

		protected Builder append(PropertyDescriptor propertyDescriptor, Object propertyValue)
		{
			return append(propertyDescriptor, propertyValue, null);
		}

		protected Builder append(PropertyDescriptor propertyDescriptor, Object propertyValue, String separationType)
		{
			return appendString(propertyDescriptor, propToString(propertyDescriptor, propertyValue, separationType), separationType);
		}

		protected Builder appendString(PropertyDescriptor propertyDescriptor, String propertyValue)
		{
			return appendString(propertyDescriptor, propertyValue, null);
		}

		protected Builder appendString(PropertyDescriptor propertyDescriptor, String propertyValue, String separationType)
		{
			if (lastProperty != null)
			{
				sb.append(SeparationDefinition.getSeparatorBetween(lastProperty, propertyDescriptor, separationType, separators));
			}
			sb.append(propertyValue);
			lastProperty = propertyDescriptor;
			return this;
		}

		protected String build()
		{
			return sb.toString();
		}
	}
}
