package com.github.walterfan.potato.common.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Walter Fan
 **/
@Slf4j
public class DateTimeUtil {
    private static DateTimeFormatter ISO8601_UTC_FORMATTER;
    private static Calendar calendar;

    static {
        ISO8601_UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        calendar = Calendar.getInstance(timeZone);
    }

    public static Date parseDate(String dateStr) {
        Date ret = null;

        try {
            ret = Date.from(Instant.parse(dateStr));
        } catch (DateTimeParseException var3) {
            log.warn("failed to parse date : {}", dateStr);
        }

        return ret;
    }

    public static String fromDate(Date date) {
        String rValue = null;

        try {
            rValue = ISO8601_UTC_FORMATTER.format(date.toInstant());
        } catch (Exception var3) {
            log.warn("failed to format date : {}", date);
        }

        return rValue;
    }

    public static Date convertDateToUTC(long date) {
        calendar.setTimeInMillis(date);
        return calendar.getTime();
    }

    public static Date convertDateToUTC(Date date) {
        calendar.setTime(date);
        return calendar.getTime();
    }

    public static TimeUnit convert(ChronoUnit tu) {
        if (tu == null) {
            return null;
        }
        switch (tu) {
            case DAYS:
                return TimeUnit.DAYS;
            case HOURS:
                return TimeUnit.HOURS;
            case MINUTES:
                return TimeUnit.MINUTES;
            case SECONDS:
                return TimeUnit.SECONDS;
            case MICROS:
                return TimeUnit.MICROSECONDS;
            case MILLIS:
                return TimeUnit.MILLISECONDS;
            case NANOS:
                return TimeUnit.NANOSECONDS;
            default:
                throw new IllegalArgumentException("Unknown temporal unit");
        }
    }

    public static ChronoUnit convert(TimeUnit tu) {
        if (tu == null) {
            return null;
        }
        switch (tu) {
            case DAYS:
                return ChronoUnit.DAYS;
            case HOURS:
                return ChronoUnit.HOURS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            default:
                throw new IllegalArgumentException("Unknown time unit");
        }
    }
}
