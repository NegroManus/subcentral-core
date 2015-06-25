package de.subcentral.core.util;

import java.lang.reflect.ParameterizedType;

public class ReflectionUtil
{
    public static Class<?> getActualTypeArg(Class<?> clazz, int index)
    {
	return (Class<?>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[index];
    }

    private ReflectionUtil()
    {
	throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }
}
