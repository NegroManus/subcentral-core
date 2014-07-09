package de.subcentral.core.naming;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.google.common.base.Joiner;

import de.subcentral.core.util.SeparatorDescriptor;

public abstract class AbstractNamer<T> implements Namer<T>
{
	protected UnaryOperator<String>							wholeNameOperator			= UnaryOperator.identity();
	protected Map<PropertyDescriptor, Function<?, String>>	propertyToStringFunctions	= new HashMap<>();
	protected Set<SeparatorDescriptor>						separators;

	public UnaryOperator<String> getWholeNameOperator()
	{
		return wholeNameOperator;
	}

	/**
	 * 
	 * @param wholeNameOperator
	 *            The operator on the whole name after it was constructed of the properties and separators.
	 * 
	 */
	public void setWholeNameOperator(UnaryOperator<String> wholeNameOperator)
	{
		this.wholeNameOperator = wholeNameOperator;
	}

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
		this.separators = separators;
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
				return Joiner.on(' ').join((Iterable<?>) property);
			}
			return property.toString();
		}
		return f.apply(property);
	}
}
