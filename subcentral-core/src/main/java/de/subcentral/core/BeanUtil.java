package de.subcentral.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Multimap;

public class BeanUtil
{
	public static final String requireNotBlankAndTrimWhitespace(String s, String message) throws IllegalArgumentException
	{
		String trimmed = StringUtils.stripToNull(s);
		if (trimmed == null)
		{
			throw new IllegalArgumentException(message);
		}
		return trimmed;
	}

	public static final Temporal validateTemporalClass(Temporal date) throws IllegalArgumentException
	{
		if (date == null || ZonedDateTime.class.equals(date.getClass()) || LocalDateTime.class.equals(date.getClass())
				|| LocalDate.class.equals(date.getClass()) || YearMonth.class.equals(date.getClass()) || Year.class.equals(date.getClass()))
		{
			return date;
		}
		throw new IllegalArgumentException("The date has to be an instance of java.time.ZonedDateTime, java.time.LocalDateTime, java.time.LocalDate, java.time.YearMonth or java.time.Year.");
	}

	public static String validateString(String str, String propertyName, String... allowedStrings) throws IllegalArgumentException
	{
		if (str == null || ArrayUtils.contains(allowedStrings, str))
		{
			return str;
		}
		throw new IllegalArgumentException(propertyName + " must be null or one of " + Arrays.toString(allowedStrings));
	}

	public static final Collection<?> nullIfEmpty(Collection<?> c)
	{
		return c == null || c.isEmpty() ? null : c;
	}

	public static final Map<?, ?> nullIfEmpty(Map<?, ?> m)
	{
		return m == null || m.isEmpty() ? null : m;
	}

	public static final Multimap<?, ?> nullIfEmpty(Multimap<?, ?> m)
	{
		return m == null || m.isEmpty() ? null : m;
	}

	public static final Integer nullIfZero(int num)
	{
		return num == 0 ? null : Integer.valueOf(num);
	}

	public static final Long nullIfZero(long num)
	{
		return num == 0L ? null : Long.valueOf(num);
	}

	public static final <T> ArrayList<T> copyToArrayList(Collection<T> c, UnaryOperator<T> elementCopier)
	{
		if (c.isEmpty())
		{
			return new ArrayList<T>(0);
		}
		ArrayList<T> copied = new ArrayList<>(c.size());
		for (T elem : c)
		{
			copied.add(elementCopier.apply(elem));
		}
		return copied;
	}

	public BeanUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
