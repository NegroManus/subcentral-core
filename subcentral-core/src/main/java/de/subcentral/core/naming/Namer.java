package de.subcentral.core.naming;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.util.StringUtil;

public interface Namer<T> extends Function<T, String>
{
    public default String name(T obj) throws NamingException
    {
	return name(obj, ImmutableMap.of());
    }

    /**
     * 
     * @param obj
     *            The object to name. Can be null.
     * @param parameters
     *            The parameters for this naming. Not null, can be empty.
     * @return The generated name of the candidate or "" if the candidate was null.
     * @throws NamingException
     *             If an Exception occurs while naming. This Exception will be the {@link Exception#getCause() cause} of the thrown NamingException.
     */
    public String name(T obj, Map<String, Object> parameters) throws NamingException;

    public default String nameAll(Iterable<? extends T> objects, String separator, Map<String, Object> parameters) throws NamingException
    {
	StringBuilder name = new StringBuilder();
	for (T candidate : objects)
	{
	    name.append(name(candidate, parameters));
	    name.append(separator);
	}
	return StringUtil.stripEnd(name, separator).toString();
    }

    public default List<String> nameEach(Iterable<? extends T> objects, Map<String, Object> parameters) throws NamingException
    {
	ImmutableList.Builder<String> names = ImmutableList.builder();
	for (T candidate : objects)
	{
	    names.add(name(candidate, parameters));
	}
	return names.build();
    }

    @Override
    public default String apply(T entity)
    {
	return name(entity);
    }
}
