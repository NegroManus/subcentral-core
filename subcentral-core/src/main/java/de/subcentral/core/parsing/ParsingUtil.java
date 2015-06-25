package de.subcentral.core.parsing;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import de.subcentral.core.util.SimplePropDescriptor;

public class ParsingUtil
{
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
			simplePropDescr.toPropertyDescriptor().getWriteMethod().invoke(entity, propFromStringService.parse(p.getValue(), simplePropDescr, type.wrap().getRawType()));
		    }

		}
		catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ParsingException e)
		{
		    e.printStackTrace();
		}
	    }
	}
    }

    public static Object parse(String text, Iterable<ParsingService> parsingServices) throws ParsingException
    {
	for (ParsingService ps : parsingServices)
	{
	    Object parsedObj = ps.parse(text);
	    if (parsedObj != null)
	    {
		return parsedObj;
	    }
	}
	return null;
    }

    public static <T> T parse(String text, Class<T> targetType, Iterable<ParsingService> parsingServices) throws ParsingException
    {
	for (ParsingService ps : parsingServices)
	{
	    T parsedObj = ps.parse(text, targetType);
	    if (parsedObj != null)
	    {
		return parsedObj;
	    }
	}
	return null;
    }

    public static Object parse(String text, Set<Class<?>> targetTypes, Iterable<ParsingService> parsingServices) throws ParsingException
    {
	for (ParsingService ps : parsingServices)
	{
	    Object parsedObj = ps.parse(text, targetTypes);
	    if (parsedObj != null)
	    {
		return parsedObj;
	    }
	}
	return null;
    }

    public static List<ParsingService> filterByTargetTypes(Iterable<ParsingService> parsingServices, Set<Class<?>> targetTypes)
    {
	ImmutableList.Builder<ParsingService> filteredServices = ImmutableList.builder();
	for (ParsingService service : parsingServices)
	{
	    if (!Collections.disjoint(service.getTargetTypes(), targetTypes))
	    {
		filteredServices.add(service);
	    }
	}
	return filteredServices.build();
    }

    private ParsingUtil()
    {
	throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }
}
