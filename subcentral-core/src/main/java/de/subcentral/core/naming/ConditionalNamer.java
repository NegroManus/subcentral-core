package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class ConditionalNamer<V> implements Namer<V>, Predicate<V>
{
	public static final <T> ConditionalNamer<T> of(Namer<T> namer)
	{
		return new AlwaysTrueConditionalNamer<T>(namer);
	}

	public static final <T> ConditionalNamer<T> of(Namer<T> namer, Predicate<T> condition)
	{
		return new RegularConditionalNamer<T>(namer, condition);
	}

	private static class AlwaysTrueConditionalNamer<U> extends ConditionalNamer<U>
	{
		private final Namer<U>	namer;

		private AlwaysTrueConditionalNamer(Namer<U> namer)
		{
			this.namer = Objects.requireNonNull(namer, "namer");
		}

		@Override
		public boolean test(U candidate)
		{
			return true;
		}

		@Override
		public String name(U candidate, Map<String, Object> parameters) throws NamingException
		{
			return namer.name(candidate, parameters);
		}
	}

	private static class RegularConditionalNamer<U> extends ConditionalNamer<U>
	{
		private final Namer<U>		namer;
		private final Predicate<U>	condition;

		private RegularConditionalNamer(Namer<U> namer, Predicate<U> condition)
		{
			this.namer = Objects.requireNonNull(namer, "namer");
			this.condition = Objects.requireNonNull(condition, "condition");
		}

		@Override
		public boolean test(U candidate)
		{
			return condition.test(candidate);
		}

		@Override
		public String name(U candidate, Map<String, Object> parameters) throws NamingException
		{
			if (condition.test(candidate))
			{
				return namer.name(candidate, parameters);
			}
			return null;
		}
	}

}
