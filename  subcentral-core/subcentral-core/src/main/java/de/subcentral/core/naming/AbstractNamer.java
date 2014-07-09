package de.subcentral.core.naming;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.jsoup.helper.Validate;

import com.google.common.base.Joiner;

import de.subcentral.core.util.SeparatorDescriptor;

public abstract class AbstractNamer<T> implements Namer<T>
{
	private Map<PropertyDescriptor, Function<?, String>>	propertyToStringFunctions	= new HashMap<>();
	private Set<SeparatorDescriptor>						separators					= new HashSet<>();
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

	public Set<SeparatorDescriptor> getSeparators()
	{
		return separators;
	}

	/**
	 * 
	 * @param separators
	 *            The separators between the properties.
	 */
	public void setSeparators(Set<SeparatorDescriptor> separators)
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

	protected <P> String propToString(PropertyDescriptor propertyDescriptor, P property)
	{
		if (property == null)
		{
			return null;
		}
		@SuppressWarnings("unchecked")
		Function<P, String> f = (Function<P, String>) propertyToStringFunctions.get(propertyDescriptor);
		if (f == null)
		{
			if (property instanceof Iterable)
			{
				return Joiner.on(getSeparatorBetween(propertyDescriptor, propertyDescriptor)).join((Iterable<?>) property);
			}
			return property.toString();
		}
		return f.apply(property);
	}

	protected String getSeparatorBetween(PropertyDescriptor firstProperty, PropertyDescriptor secondProperty)
	{
		return SeparatorDescriptor.getSeparatorBetween(firstProperty, secondProperty, separators);
	}
}
