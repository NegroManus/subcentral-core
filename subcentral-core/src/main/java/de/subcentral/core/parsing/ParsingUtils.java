package de.subcentral.core.parsing;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import de.subcentral.core.util.SimplePropDescriptor;

public class ParsingUtils
{
	public static void requireNotBlank(String text, Class<?> targetClass) throws NoMatchException
	{
		if (StringUtils.isBlank(text))
		{
			throw new NoMatchException(text, targetClass, "text is blank");
		}
	}

	public static final <T> void reflectiveMapping(T entity, Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService)
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
					TypeToken<?> type = TypeToken.of(propDescr.getReadMethod().getGenericParameterTypes()[0]);
					if (Collection.class.isAssignableFrom(type.getRawType()))
					{
						ParameterizedType genericType = (ParameterizedType) type.getType();
						Class<?> itemClass = ((Class<?>) genericType.getActualTypeArguments()[0]);
						List<?> value = propFromStringService.parseList(p.getValue(), simplePropDescr, itemClass);
						if (Set.class.isAssignableFrom(type.getRawType()))
						{
							simplePropDescr.toPropertyDescriptor().getWriteMethod().invoke(entity, ImmutableSet.copyOf(value));
						}
						else
						{
							simplePropDescr.toPropertyDescriptor().getWriteMethod().invoke(entity, value);
						}
					}
					else
					{
						simplePropDescr.toPropertyDescriptor()
								.getWriteMethod()
								.invoke(entity, propFromStringService.parse(p.getValue(), simplePropDescr, type.wrap().getRawType()));
					}

				}
				catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ParsingException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static <T> T parse(String text, Class<T> targetClass, List<ParsingService> parsingServices) throws NoMatchException, ParsingException
	{
		for (ParsingService ps : parsingServices)
		{
			try
			{
				return ps.parse(text, targetClass);
			}
			catch (NoMatchException nme)
			{
				continue;
			}
		}
		throw new NoMatchException(text, targetClass, "No ParsingService could parse the text");
	}

	public static Object parse(String text, List<ParsingService> parsingServices) throws NoMatchException, ParsingException
	{
		for (ParsingService ps : parsingServices)
		{
			try
			{
				return ps.parse(text);
			}
			catch (NoMatchException nme)
			{
				continue;
			}
		}
		throw new NoMatchException(text, null, "No ParsingService could parse the text");
	}

	private ParsingUtils()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
