package de.subcentral.core.parsing;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.common.reflect.TypeToken;

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

	public static void requireTextNotBlank(String text) throws ParsingException
	{
		if (StringUtils.isBlank(text))
		{
			throw new ParsingException("Could not parse text because it is blank: " + (text == null ? "null" : "'" + text + "'"));
		}
	}

	public static <T> void reflectiveMapping(T entity, Map<SimplePropDescriptor, String> props, PropParsingService pps)
	{
		Objects.requireNonNull(entity, "entity");
		for (Map.Entry<SimplePropDescriptor, String> p : props.entrySet())
		{
			SimplePropDescriptor simplePropDescr = p.getKey();
			if (entity.getClass().equals(simplePropDescr.getBeanClass()))
			{
				try
				{
					PropertyDescriptor propDescr = simplePropDescr.toPropertyDescriptor();
					TypeToken<?> type = TypeToken.of(propDescr.getReadMethod().getGenericReturnType());
					if (Collection.class.isAssignableFrom(type.getRawType()))
					{
						ParameterizedType genericType = (ParameterizedType) type.getType();
						Class<?> itemClass = ((Class<?>) genericType.getActualTypeArguments()[0]);
						simplePropDescr.toPropertyDescriptor()
								.getWriteMethod()
								.invoke(entity, pps.parseList(p.getValue(), simplePropDescr, itemClass));
					}
					else
					{
						simplePropDescr.toPropertyDescriptor()
								.getWriteMethod()
								.invoke(entity, pps.parse(p.getValue(), simplePropDescr, type.wrap().getRawType()));
					}

				}
				catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ParsingException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static <T> T tryMap(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService, List<ConditionalMapper<T>> mappers)
			throws MappingException
	{
		for (ConditionalMapper<T> m : mappers)
		{
			try
			{
				return m.map(props, propParsingService);
			}
			catch (MappingException e)
			{
				// ignore and move on
			}
		}
		throw new MappingException("No conditional mapper could map the properties: " + props);
	}

	private Parsings()
	{
		// util class
	}

}
