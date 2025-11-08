package com.pulse.fineflux.utill;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Centralized date/time utilities to ensure all timestamps use IST (Asia/Kolkata).
 */
public final class DateTimeUtil {
    private DateTimeUtil() {}

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    public static LocalDateTime nowLocal() {
        return LocalDateTime.now(IST).truncatedTo(ChronoUnit.SECONDS);
    }

    public static ZonedDateTime nowZoned() {
        return ZonedDateTime.now(IST).truncatedTo(ChronoUnit.SECONDS);
    }

    public static Instant nowInstant() {
        return nowZoned().toInstant();
    }

    public static Date nowDate() {
        return Date.from(nowInstant());
    }

    public static LocalDateTime toIst(LocalDateTime source, ZoneId sourceZone) {
        if (source == null) return null;
        return source.atZone(sourceZone).withZoneSameInstant(IST).toLocalDateTime();
    }

    public static LocalDateTime toIstFromZoned(ZonedDateTime zdt) {
        if (zdt == null) return null;
        return zdt.withZoneSameInstant(IST).toLocalDateTime();
    }

    public static Instant toInstant(LocalDateTime ldt, ZoneId zone) {
        if (ldt == null) return null;
        return ldt.atZone(zone).toInstant();
    }
}
