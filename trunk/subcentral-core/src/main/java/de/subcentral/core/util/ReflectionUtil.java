package de.subcentral.core.util;

import java.lang.reflect.ParameterizedType;

public class ReflectionUtil
{
	public static Class<?> getFirstTypeArg(Class<?> clazz)
	{
		return (Class<?>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	private ReflectionUtil()
	{
		// utilty class
	}

}
