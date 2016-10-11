package de.subcentral.core.util;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.util.Comparator;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

public class TemporalComparator implements Comparator<Temporal>, Serializable {
    public static final Comparator<Temporal> INSTANCE         = new TemporalComparator();

    // Comparators should be Serializable
    private static final long                serialVersionUID = -7918244215818591537L;

    private TemporalComparator() {
        // singleton: not instantiable from outside
    }

    @Override
    public int compare(Temporal o1, Temporal o2) {
        // nulls first
        if (o1 == null) {
            return o2 == null ? 0 : -1;
        }
        if (o2 == null) {
            return 1;
        }

        Temporal o1Zoned = toSystemDefaultZonedDateTimeIfInstant(o1);
        Temporal o2Zoned = toSystemDefaultZonedDateTimeIfInstant(o2);
        Temporal o1Utc = toUtc(o1Zoned);
        Temporal o2Utc = toUtc(o2Zoned);

        Integer year1 = getOrNull(o1Utc, ChronoField.YEAR);
        Integer year2 = getOrNull(o2Utc, ChronoField.YEAR);
        Integer month1 = getOrNull(o1Utc, ChronoField.MONTH_OF_YEAR);
        Integer month2 = getOrNull(o2Utc, ChronoField.MONTH_OF_YEAR);
        Integer day1 = getOrNull(o1Utc, ChronoField.DAY_OF_YEAR);
        Integer day2 = getOrNull(o2Utc, ChronoField.DAY_OF_YEAR);
        Long nano1 = getLongOrNull(o1Utc, ChronoField.NANO_OF_DAY);
        Long nano2 = getLongOrNull(o2Utc, ChronoField.NANO_OF_DAY);

        Ordering<Integer> intOrdering = ObjectUtil.getDefaultOrdering();
        Ordering<Long> longOrdering = ObjectUtil.getDefaultOrdering();
        return ComparisonChain.start().compare(year1, year2, intOrdering).compare(month1, month2, intOrdering).compare(day1, day2, intOrdering).compare(nano1, nano2, longOrdering).result();
    }

    private static final Temporal toSystemDefaultZonedDateTimeIfInstant(Temporal t) {
        if (Instant.class == t.getClass()) {
            return ((Instant) t).atZone(ZoneId.systemDefault());
        }
        return t;
    }

    private static final Temporal toUtc(Temporal t) {
        Long offset = getLongOrNull(t, ChronoField.OFFSET_SECONDS);
        if (offset == null) {
            return t;
        }
        return t.minus(offset, ChronoUnit.SECONDS);
    }

    private static final Integer getOrNull(Temporal t, TemporalField field) {
        if (t.isSupported(field)) {
            return t.get(field);
        }
        return null;
    }

    private static final Long getLongOrNull(Temporal t, TemporalField field) {
        if (t.isSupported(field)) {
            return t.getLong(field);
        }
        return null;
    }
}
