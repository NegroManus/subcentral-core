package de.subcentral.core.parsing;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public class Parsings
{
	/**
	 * Pattern for media names like "The Lord of the Rings (2003)", "The Office (UK)".<br/>
	 * Groups
	 * <ol>
	 * <li>name</li>
	 * <li>title (may be equal to name)</li>
	 * <li>(optional year / country group)</li>
	 * <li>(either year or country group)</li>
	 * <li>year (or null)</li>
	 * <li>country code (or null)</li>
	 * </ol>
	 */
	public static final String	PATTERN_MEDIA_NAME	= "((.*?)(\\s+\\(((\\d{4})|(\\p{Upper}{2}))\\))?)";

	public static final <T> void setAllProperties(T bean, Class<T> beanClass, Map<SimplePropDescriptor, String> properties, PropParsingService pps)
	{
		for (Map.Entry<SimplePropDescriptor, String> entry : properties.entrySet())
		{
			SimplePropDescriptor prop = entry.getKey();
			if (beanClass.equals(prop.getBeanClass()))
			{
				try
				{
					PropertyDescriptor propDescr = prop.toPropertyDescriptor();
					propDescr.getWriteMethod().invoke(bean, pps.parse(entry.getValue(), prop, propDescr.getPropertyType()));
				}
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException | ParsingException e)
				{
					System.err.println("Could not set property " + prop + " to " + entry.getValue());
					e.printStackTrace();
				}
			}
		}
	}

	private Parsings()
	{
		// util class
	}

}
