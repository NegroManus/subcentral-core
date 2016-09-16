package de.subcentral.core.name;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import com.google.common.base.MoreObjects;

import de.subcentral.core.util.Context;

/**
 * {@code Thread-safe}
 */
public class ConditionalNamingService implements NamingService {
	private final String				name;
	private final List<NamerEntry<?>>	entries	= new CopyOnWriteArrayList<>();

	public ConditionalNamingService(String name) {
		this.name = Objects.requireNonNull(name, "name");
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Important:
	 * <ul>
	 * <li>The order of the elements is the order the conditions are tested. So more restricting conditions must be placed before more general conditions. The first NamerEntry which condition returns
	 * true will be taken.</li>
	 * <li>The order should also consider how often specific types are named. The types that are named most frequently should be at the top of the list.</li>
	 * </ul>
	 * 
	 * @return
	 */
	public List<NamerEntry<?>> getEntries() {
		return entries;
	}

	public <U> void register(Predicate<Object> condition, Namer<U> namer) {
		entries.add(new NamerEntry<U>(condition, namer));
	}

	@Override
	public String name(Object obj, Context ctx) {
		return nameTyped(obj, ctx);
	}

	private final <T> String nameTyped(T obj, Context ctx) {
		Namer<? super T> namer = getNamer(obj);
		if (namer != null) {
			return namer.name(obj, ctx);
		}
		if (obj instanceof Iterable) {
			return nameAll((Iterable<?>) obj, ctx);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T> Namer<? super T> getNamer(T candidate) {
		if (candidate == null) {
			return null;
		}
		for (NamerEntry<?> e : entries) {
			if (e.test(candidate)) {
				return (Namer<? super T>) e.getNamer();
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(ConditionalNamingService.class).add("name", name).toString();
	}

	public static class NamerEntry<U> implements Predicate<Object> {
		private final Predicate<Object>	condition;
		private final Namer<U>			namer;

		private NamerEntry(Predicate<Object> condition, Namer<U> namer) {
			this.condition = Objects.requireNonNull(condition, "condition");
			this.namer = Objects.requireNonNull(namer, "namer");
		}

		public static <V> NamerEntry<V> of(Predicate<Object> condition, Namer<V> namer) {
			return new NamerEntry<>(condition, namer);
		}

		public Predicate<Object> getCondition() {
			return condition;
		}

		public Namer<U> getNamer() {
			return namer;
		}

		@Override
		public boolean test(Object obj) {
			return condition.test(obj);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(NamerEntry.class).add("condition", condition).add("namer", namer).toString();
		}
	}
}
