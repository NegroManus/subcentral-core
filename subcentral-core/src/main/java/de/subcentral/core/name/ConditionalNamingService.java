package de.subcentral.core.name;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import com.google.common.base.MoreObjects;

import de.subcentral.core.util.Separation;

/**
 * {@code Thread-safe}
 */
public class ConditionalNamingService implements NamingService
{
	private final String							domain;
	private final List<ConditionalNamingEntry<?>>	entries				= new CopyOnWriteArrayList<>();
	private final AtomicReference<String>			defaultSeparator	= new AtomicReference<>(Separation.DEFAULT_SEPARATOR);

	public ConditionalNamingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	/**
	 * Important:
	 * <ul>
	 * <li>The order of the elements is the order the conditions are tested. So more restricting conditions must be placed before more general conditions. The first ConditionalNamingEntry which
	 * condition returns true will be taken.</li>
	 * <li>The order should also consider how often specific types are named. The types that are named most frequently should be at the top of the list.</li>
	 * </ul>
	 * 
	 * @return
	 */
	public List<ConditionalNamingEntry<?>> getConditionalNamingEntries()
	{
		return entries;
	}

	@SuppressWarnings("unchecked")
	public <V> ConditionalNamingEntry<V> getEntryFor(Namer<V> namer)
	{
		for (ConditionalNamingEntry<?> e : entries)
		{
			if (e.getNamer().equals(namer))
			{
				return (ConditionalNamingEntry<V>) e;
			}
		}
		return null;
	}

	public ConditionalNamingEntry<?> getEntryFor(Predicate<Object> condition)
	{
		for (ConditionalNamingEntry<?> e : entries)
		{
			if (e.getCondition().equals(condition))
			{
				return e;
			}
		}
		return null;
	}

	@Override
	public String getDefaultSeparator()
	{
		return defaultSeparator.get();
	}

	public void setDefaultSeparator(String defaultSeparator)
	{
		this.defaultSeparator.set(defaultSeparator != null ? defaultSeparator : Separation.DEFAULT_SEPARATOR);
	}

	@SuppressWarnings("unchecked")
	public <T> Namer<? super T> getNamer(T candidate) throws ClassCastException
	{
		if (candidate == null)
		{
			return null;
		}
		for (ConditionalNamingEntry<?> e : entries)
		{
			if (e.test(candidate))
			{
				return (Namer<? super T>) e.getNamer();
			}
		}
		return null;
	}

	@Override
	public String name(Object candidate, Map<String, Object> parameters) throws NoNamerRegisteredException, NamingException
	{
		return doName(candidate, parameters);
	}

	private final <T> String doName(T candidate, Map<String, Object> parameters) throws NoNamerRegisteredException, NamingException
	{
		Namer<? super T> namer = getNamer(candidate);
		if (namer != null)
		{
			return namer.name(candidate, parameters);
		}
		if (candidate instanceof Iterable)
		{
			return nameAll((Iterable<?>) candidate, parameters);
		}
		throw new NoNamerRegisteredException(candidate, "No ConditionalNamingEntry's condition returned true for the candidate");
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(ConditionalNamingService.class).add("domain", domain).toString();
	}

	public static class ConditionalNamingEntry<U>
	{
		private final Predicate<Object>	condition;
		private final Namer<U>			namer;

		public static <V> ConditionalNamingEntry<V> of(Predicate<Object> condition, Namer<V> namer)
		{
			Objects.requireNonNull(condition, "condition");
			return new ConditionalNamingEntry<V>(condition, namer);
		}

		private ConditionalNamingEntry(Predicate<Object> condition, Namer<U> namer)
		{
			this.condition = Objects.requireNonNull(condition, "condition");
			this.namer = Objects.requireNonNull(namer, "namer");
		}

		public Predicate<Object> getCondition()
		{
			return condition;
		}

		public Namer<U> getNamer()
		{
			return namer;
		}

		public boolean test(Object obj)
		{
			return condition.test(obj);
		}
	}
}
