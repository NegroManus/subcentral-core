package de.subcentral.core.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimeUtil {
    private static final Logger log = LogManager.getLogger(TimeUtil.class);

    private TimeUtil() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

    public static void logDurationMillis(String operation, long startNanos) {
        log.info("Duration of {}: {} ms", operation, durationMillis(startNanos));
    }

    public static long durationMillis(long startNanos) {
        return durationMillis(startNanos, System.nanoTime());
    }

    public static long durationMillis(long startNanos, long endNanos) {
        return (endNanos - startNanos) / 1_000_000L;
    }

    public static void logDurationMillisDouble(String operation, long startNanos) {
        log.info("Duration of {}: {} ms", operation, durationMillisDouble(startNanos));
    }

    public static double durationMillisDouble(long startNanos) {
        return durationMillisDouble(startNanos, System.nanoTime());
    }

    public static double durationMillisDouble(long startNanos, long endNanos) {
        return (endNanos - startNanos) / 1_000_000d;
    }

    public static Year getYear(Temporal t) {
        if (t == null) {
            return null;
        }
        try {
            return Year.from(t);
        }
        catch (DateTimeException e) {
            log.trace("Exception while getting Year of temporal " + t, e);
            return null;
        }
    }

    public static Temporal parseTemporal(String s) throws DateTimeParseException {
        try {
            return ZonedDateTime.parse(s);
        }
        catch (Exception e) {
            // ignore
        }
        try {
            return LocalDateTime.parse(s);
        }
        catch (Exception e) {
            // ignore
        }
        try {
            return LocalDate.parse(s);
        }
        catch (Exception e) {
            // ignore
        }
        try {
            return YearMonth.parse(s);
        }
        catch (Exception e) {
            // ignore
        }
        try {
            return Year.parse(s);
        }
        catch (Exception e) {
            throw new DateTimeParseException("Text '" + s + "' could not be parsed to any temporal type", s, 0);
        }
    }
}
