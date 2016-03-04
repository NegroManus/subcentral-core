package de.subcentral.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

import org.apache.commons.lang3.StringUtils;

public class ValidationUtil
{
	private ValidationUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static String requireNotBlankAndStrip(String s, String message) throws IllegalArgumentException
	{
		String trimmed = StringUtils.stripToNull(s);
		if (trimmed != null)
		{
			return trimmed;
		}
		throw new IllegalArgumentException(message);
	}

	public static Temporal validateTemporalClass(Temporal date) throws IllegalArgumentException
	{
		if (date == null || ZonedDateTime.class.equals(date.getClass()) || LocalDateTime.class.equals(date.getClass()) || LocalDate.class.equals(date.getClass())
				|| YearMonth.class.equals(date.getClass()) || Year.class.equals(date.getClass()))
		{
			return date;
		}
		throw new IllegalArgumentException("The date has to be an instance of java.time.ZonedDateTime, java.time.LocalDateTime, java.time.LocalDate, java.time.YearMonth or java.time.Year.");
	}
}
