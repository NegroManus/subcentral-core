package de.subcentral.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.UnaryOperator;

public class Models
{
	public static final Temporal validateTemporalClass(Temporal date) throws IllegalArgumentException
	{
		if (date == null || ZonedDateTime.class.equals(date.getClass()) || LocalDateTime.class.equals(date.getClass())
				|| LocalDate.class.equals(date.getClass()) || YearMonth.class.equals(date.getClass()) || Year.class.equals(date.getClass()))
		{
			return date;
		}
		throw new IllegalArgumentException("The date has to be an instance of java.time.ZonedDateTime, java.time.LocalDateTime, java.time.LocalDate, java.time.YearMonth or java.time.Year.");
	}

	public static final Collection<?> nullIfEmpty(Collection<?> c)
	{
		return c.isEmpty() ? null : c;
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

	public Models()
	{
		// utility class
	}
}
