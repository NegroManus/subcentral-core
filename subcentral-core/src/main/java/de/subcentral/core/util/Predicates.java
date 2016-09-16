package de.subcentral.core.util;

import java.util.Objects;
import java.util.function.Predicate;

public class Predicates {
	private Predicates() {
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static Predicate<Object> instanceOf(Class<?> type) {
		Objects.requireNonNull(type, "type");
		return type::isInstance;
	}

	public static Predicate<Object> typeEquals(Class<?> type) {
		Objects.requireNonNull(type, "type");
		return (Object o) -> o != null && type.equals(o.getClass());
	}
}
