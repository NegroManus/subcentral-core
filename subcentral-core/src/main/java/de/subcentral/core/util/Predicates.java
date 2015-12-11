package de.subcentral.core.util;

import java.util.Objects;
import java.util.function.Predicate;

public class Predicates
{
	public static Predicate<Object> instanceOf(Class<?> type)
	{
		Objects.requireNonNull(type, "type");
		return (Object o) -> type.isInstance(o);
	}

	public static Predicate<Object> typeEquals(Class<?> type)
	{
		Objects.requireNonNull(type, "type");
		return (Object o) -> o != null && type.equals(o.getClass());
	}
}
