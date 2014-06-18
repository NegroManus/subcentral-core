package de.subcentral.core.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.util.Comparator;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

public class TemporalComparator implements Comparator<Temporal>
{
	@Override
	public int compare(Temporal o1, Temporal o2)
	{
		if (o1 == null)
		{
			return o2 == null ? 0 : 1;
		}
		if (o2 == null)
		{
			return -1;
		}

		Temporal converted1 = toSystemDefaultZonedDateTimeIfInstant(o1);
		Temporal converted2 = toSystemDefaultZonedDateTimeIfInstant(o2);

		Temporal o1Utc = toUtc(converted1);
		Temporal o2Utc = toUtc(converted2);

		Integer year1 = getOrNull(o1Utc, ChronoField.YEAR);
		Integer year2 = getOrNull(o2Utc, ChronoField.YEAR);
		Integer day1 = getOrNull(o1Utc, ChronoField.DAY_OF_YEAR);
		Integer day2 = getOrNull(o2Utc, ChronoField.DAY_OF_YEAR);
		Long nano1 = getLongOrNull(o1Utc, ChronoField.NANO_OF_DAY);
		Long nano2 = getLongOrNull(o2Utc, ChronoField.NANO_OF_DAY);

		return ComparisonChain.start()
				.compare(year1, year2, Ordering.natural().nullsLast())
				.compare(day1, day2, Ordering.natural().nullsLast())
				.compare(nano1, nano2, Ordering.natural().nullsLast())
				.result();
	}

	private static Temporal toSystemDefaultZonedDateTimeIfInstant(Temporal t)
	{
		if (Instant.class == t.getClass())
		{
			return ((Instant) t).atZone(ZoneId.systemDefault());
		}
		return t;
	}

	private static Temporal toUtc(Temporal t)
	{
		Long offset = getLongOrNull(t, ChronoField.OFFSET_SECONDS);
		if (offset == null)
		{
			return t;
		}
		return t.minus(offset, ChronoUnit.SECONDS);
	}

	private static Integer getOrNull(Temporal t, TemporalField field)
	{
		if (t.isSupported(field))
		{
			return t.get(field);
		}
		return null;
	}

	private static Long getLongOrNull(Temporal t, TemporalField field)
	{
		if (t.isSupported(field))
		{
			return t.getLong(field);
		}
		return null;
	}
}
