package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.google.common.base.MoreObjects;

public abstract class ConditionalNamer<V> implements Namer<V>, Predicate<Object>
{
	public String mayName(V candidate, Map<String, Object> parameters) throws NamingException
	{
		if (test(candidate))
		{
			return name(candidate, parameters);
		}
		return null;
	}

	public static final <T> ConditionalNamer<T> withInstanceOf(Namer<T> namer, Class<? extends T> requiredClass)
	{
		return new InstanceOfConditionalNamer<T>(namer, requiredClass);
	}

	public static final <T> ConditionalNamer<T> with(Namer<T> namer, Predicate<Object> condition)
	{
		return new RegularConditionalNamer<T>(namer, condition);
	}

	private static class InstanceOfConditionalNamer<U> extends ConditionalNamer<U>
	{
		private final Namer<U>				namer;
		private final Class<? extends U>	requiredClass;

		private InstanceOfConditionalNamer(Namer<U> namer, Class<? extends U> requiredClass)
		{
			this.namer = Objects.requireNonNull(namer, "namer");
			this.requiredClass = Objects.requireNonNull(requiredClass, "requiredClass");
		}

		@Override
		public boolean test(Object candidate)
		{
			return requiredClass.isInstance(candidate);
		}

		@Override
		public String name(U candidate, Map<String, Object> parameters) throws NamingException
		{
			return namer.name(candidate, parameters);
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(InstanceOfConditionalNamer.class)
					.omitNullValues()
					.add("namer", namer)
					.add("requiredClass", requiredClass)
					.toString();
		}
	}

	private static class RegularConditionalNamer<U> extends ConditionalNamer<U>
	{
		private final Namer<U>			namer;
		private final Predicate<Object>	condition;

		private RegularConditionalNamer(Namer<U> namer, Predicate<Object> condition)
		{
			this.namer = Objects.requireNonNull(namer, "namer");
			this.condition = Objects.requireNonNull(condition, "condition");
		}

		@Override
		public boolean test(Object candidate)
		{
			return condition.test(candidate);
		}

		@Override
		public String name(U candidate, Map<String, Object> parameters) throws NamingException
		{
			return namer.name(candidate, parameters);
		}
	}
}
