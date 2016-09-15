package de.subcentral.core.util;

import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;

public class ObjectUtil {
	private static final Ordering<String>	DEFAULT_STRING_ORDERING		= getDefaultOrdering(Collator.getInstance());
	private static final Ordering<Pattern>	DEFAULT_PATTERN_ORDERING	= getDefaultOrdering(initPatternComparator());

	private ObjectUtil() {
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	private static Comparator<Pattern> initPatternComparator() {
		return (Pattern p1, Pattern p2) -> {
			return ComparisonChain.start().compare(p1.pattern(), p2.pattern()).compare(p1.flags(), p2.flags()).result();
		};
	}

	public static boolean stringEqualIgnoreCase(String s1, String s2) {
		return (s1 == s2) || (s1 != null && s1.equalsIgnoreCase(s2));
	}

	public static int stringHashCodeIgnoreCase(String s) {
		return s != null ? s.toLowerCase().hashCode() : 0;
	}

	public static boolean patternsEqual(Pattern p1, Pattern p2) {
		return (p1 == p2) || (p1 != null && p1.pattern().equals(p2.pattern()) && p1.flags() == p2.flags());
	}

	public static int patternHashCode(Pattern p) {
		return Objects.hash(p.pattern(), p.flags());
	}

	public static <E> Collection<E> nullIfEmpty(Collection<E> c) {
		return c == null || c.isEmpty() ? null : c;
	}

	public static <K, V> Map<K, V> nullIfEmpty(Map<K, V> m) {
		return m == null || m.isEmpty() ? null : m;
	}

	public static <K, V> Multimap<K, V> nullIfEmpty(Multimap<K, V> m) {
		return m == null || m.isEmpty() ? null : m;
	}

	public static Integer nullIfZero(int num) {
		return num == 0 ? null : Integer.valueOf(num);
	}

	public static Long nullIfZero(long num) {
		return num == 0L ? null : Long.valueOf(num);
	}

	public static <T extends Comparable<T>> Ordering<T> getDefaultOrdering() {
		return Ordering.natural().nullsFirst();
	}

	public static <T> Ordering<T> getDefaultOrdering(Comparator<? super T> comparator) {
		return Ordering.from(comparator).nullsFirst();
	}

	public static Ordering<String> getDefaultStringOrdering() {
		return DEFAULT_STRING_ORDERING;
	}

	public static Ordering<Pattern> getDefaultPatternOrdering() {
		return DEFAULT_PATTERN_ORDERING;
	}

	public static String toString(Object obj) {
		return obj != null ? obj.toString() : "";
	}

	public static <T, U> Optional<U> convertOptional(Optional<T> opt, Function<T, U> converter) {
		return opt.isPresent() ? Optional.of(converter.apply(opt.get())) : Optional.empty();
	}
}
