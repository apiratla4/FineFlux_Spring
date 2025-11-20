package com.pulse.fineflux.service;

import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Service for handling date/time operations with IST (Asia/Kolkata) timezone.
 * This ensures consistent timezone handling across the application.
 *
 * All dates are stored in MongoDB in IST timezone.
 * Frontend dates (from any timezone) are automatically converted to IST.
 */
@Service
public class DateTimeService {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Get current LocalDateTime in IST
     */
    public LocalDateTime nowLocal() {
        return LocalDateTime.now(IST).truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Get current ZonedDateTime in IST
     */
    public ZonedDateTime nowZoned() {
        return ZonedDateTime.now(IST).truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Get current Instant
     */
    public Instant nowInstant() {
        return nowZoned().toInstant();
    }

    /**
     * Get current Date in IST
     */
    public Date nowDate() {
        return Date.from(nowInstant());
    }

    /**
     * Convert LocalDateTime from any timezone to IST
     * @param source LocalDateTime from frontend
     * @param sourceZone The timezone of the source (e.g., ZoneId.of("America/New_York"))
     * @return LocalDateTime in IST
     */
    public LocalDateTime toIstFromLocalDateTime(LocalDateTime source, ZoneId sourceZone) {
        if (source == null) return null;
        return source.atZone(sourceZone).withZoneSameInstant(IST).toLocalDateTime();
    }

    /**
     * Convert ZonedDateTime from any timezone to IST LocalDateTime
     * @param zdt ZonedDateTime from frontend
     * @return LocalDateTime in IST
     */
    public LocalDateTime toIstFromZonedDateTime(ZonedDateTime zdt) {
        if (zdt == null) return null;
        return zdt.withZoneSameInstant(IST).toLocalDateTime();
    }

    /**
     * Convert OffsetDateTime from any timezone to IST LocalDateTime
     * @param odt OffsetDateTime from frontend
     * @return LocalDateTime in IST
     */
    public LocalDateTime toIstFromOffsetDateTime(OffsetDateTime odt) {
        if (odt == null) return null;
        return odt.atZoneSameInstant(IST).toLocalDateTime();
    }

    /**
     * Convert ISO 8601 string from frontend to IST LocalDateTime
     * Handles strings like: "2025-11-20T10:30:00Z" or "2025-11-20T10:30:00+05:30"
     * @param isoDateTimeString ISO 8601 formatted date-time string
     * @return LocalDateTime in IST
     */
    public LocalDateTime parseIsoToIst(String isoDateTimeString) {
        if (isoDateTimeString == null || isoDateTimeString.isEmpty()) return null;

        try {
            // Try parsing as ZonedDateTime first (handles timezone info)
            ZonedDateTime zdt = ZonedDateTime.parse(isoDateTimeString, ISO_FORMATTER);
            return toIstFromZonedDateTime(zdt);
        } catch (Exception e1) {
            try {
                // Try parsing as OffsetDateTime
                OffsetDateTime odt = OffsetDateTime.parse(isoDateTimeString, ISO_FORMATTER);
                return toIstFromOffsetDateTime(odt);
            } catch (Exception e2) {
                try {
                    // Try parsing as LocalDateTime (assume IST if no timezone)
                    return LocalDateTime.parse(isoDateTimeString, ISO_FORMATTER);
                } catch (Exception e3) {
                    throw new IllegalArgumentException("Unable to parse date-time string: " + isoDateTimeString, e3);
                }
            }
        }
    }

    /**
     * Convert LocalDateTime in IST to ISO 8601 string with IST timezone
     * @param ldt LocalDateTime in IST
     * @return ISO 8601 formatted string with IST timezone
     */
    public String toIsoString(LocalDateTime ldt) {
        if (ldt == null) return null;
        return ldt.atZone(IST).format(ISO_FORMATTER);
    }

    /**
     * Convert LocalDateTime in IST to display format
     * @param ldt LocalDateTime in IST
     * @return Formatted string like "2025-11-20 10:30:00"
     */
    public String toDisplayString(LocalDateTime ldt) {
        if (ldt == null) return null;
        return ldt.format(DISPLAY_FORMATTER);
    }

    /**
     * Convert Instant to LocalDateTime in IST
     * @param instant Instant value
     * @return LocalDateTime in IST
     */
    public LocalDateTime toIstFromInstant(Instant instant) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, IST);
    }

    /**
     * Convert LocalDateTime in IST to Instant
     * @param ldt LocalDateTime in IST
     * @return Instant
     */
    public Instant toInstant(LocalDateTime ldt) {
        if (ldt == null) return null;
        return ldt.atZone(IST).toInstant();
    }

    /**
     * Check if a date is today in IST
     * @param ldt LocalDateTime to check
     * @return true if the date is today in IST
     */
    public boolean isToday(LocalDateTime ldt) {
        if (ldt == null) return false;
        LocalDate today = LocalDate.now(IST);
        return ldt.toLocalDate().equals(today);
    }

    /**
     * Get start of day in IST for a given LocalDateTime
     * @param ldt LocalDateTime
     * @return LocalDateTime at start of day (00:00:00)
     */
    public LocalDateTime getStartOfDay(LocalDateTime ldt) {
        if (ldt == null) return null;
        return ldt.toLocalDate().atStartOfDay();
    }

    /**
     * Get end of day in IST for a given LocalDateTime
     * @param ldt LocalDateTime
     * @return LocalDateTime at end of day (23:59:59)
     */
    public LocalDateTime getEndOfDay(LocalDateTime ldt) {
        if (ldt == null) return null;
        return ldt.toLocalDate().atTime(23, 59, 59);
    }

    /**
     * Calculate days between two LocalDateTime values
     * @param start Start LocalDateTime
     * @param end End LocalDateTime
     * @return Number of days between the two dates
     */
    public long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }

    /**
     * Parse date string in various formats and convert to IST LocalDateTime
     * Supports: "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", ISO 8601
     * @param dateString Date string in various formats
     * @return LocalDateTime in IST
     */
    public LocalDateTime parseFlexible(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null;

        // Try ISO format first
        try {
            return parseIsoToIst(dateString);
        } catch (Exception e1) {
            // Try display format
            try {
                return LocalDateTime.parse(dateString, DISPLAY_FORMATTER);
            } catch (Exception e2) {
                // Try date only format (assume start of day)
                try {
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate date = LocalDate.parse(dateString, dateFormatter);
                    return date.atStartOfDay();
                } catch (Exception e3) {
                    throw new IllegalArgumentException("Unable to parse date string: " + dateString, e3);
                }
            }
        }
    }
}

