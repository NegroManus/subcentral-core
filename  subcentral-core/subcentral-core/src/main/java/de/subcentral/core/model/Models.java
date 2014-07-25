package de.subcentral.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Collection;

public class Models
{
	public static final void validateTemporalClass(Temporal date) throws IllegalArgumentException
	{
		if (date == null || ZonedDateTime.class.equals(date.getClass()) || LocalDateTime.class.equals(date.getClass())
				|| LocalDate.class.equals(date.getClass()) || Year.class.equals(date.getClass()))
		{
			return;
		}
		throw new IllegalArgumentException("The date has to be an instance of java.time.ZonedDateTime, java.time.LocalDateTime, java.time.LocalDate or java.time.Year.");
	}

	public static final Collection<?> nullIfEmpty(Collection<?> c)
	{
		return c.isEmpty() ? null : c;
	}

	public Models()
	{
		// utility class
	}

}
