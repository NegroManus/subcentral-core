package de.subcentral.core.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;

public class TimeUtil
{
	public static void printDurationMillis(long startNanos)
	{
		System.out.println(durationMillis(startNanos));
	}

	public static double durationMillis(long startNanos)
	{
		return durationMillis(startNanos, System.nanoTime());
	}

	public static double durationMillis(long startNanos, long endNanos)
	{
		return (endNanos - startNanos) / 1_000_000d;
	}

	public static Year getYear(Temporal t)
	{
		if (t == null)
		{
			return null;
		}
		try
		{
			return Year.of(t.get(ChronoField.YEAR));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static Temporal parseTemporal(String s) throws DateTimeParseException
	{
		try
		{
			return ZonedDateTime.parse(s);
		}
		catch (Exception e)
		{
			// ignore
		}
		try
		{
			return LocalDateTime.parse(s);
		}
		catch (Exception e)
		{
			// ignore
		}
		try
		{
			return LocalDate.parse(s);
		}
		catch (Exception e)
		{
			// ignore
		}
		try
		{
			return YearMonth.parse(s);
		}
		catch (Exception e)
		{
			// ignore
		}
		try
		{
			return Year.parse(s);
		}
		catch (Exception e)
		{
			throw new DateTimeParseException("Text '" + s + "' could not be parsed to any temporal type", s, 0);
		}
	}

	public static void main(String[] args)
	{
		System.out.println(durationMillis(1_000_000, 2_000_000));
	}

	private TimeUtil()
	{
		// utilty class
	}

}
