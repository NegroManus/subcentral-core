package de.subcentral.core.naming;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.jsoup.helper.Validate;

import de.subcentral.core.util.SeparationDefinition;
import de.subcentral.core.util.SimplePropDescriptor;

public class NameBuilderFactory
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

	public NameBuilder newBuilder()
	{
		synchronized (this)
		{
			// no changes to the settings can happen while creating a new Builder
			return new NameBuilder(this);
		}
	}
}
