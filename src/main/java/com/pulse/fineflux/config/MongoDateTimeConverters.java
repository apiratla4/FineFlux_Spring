package com.pulse.fineflux.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

import java.time.*;
import java.util.Date;

/**
 * MongoDB converters to ensure all date/time values are stored in IST (Asia/Kolkata) timezone.
 * MongoDB stores dates as UTC milliseconds, but we interpret them as IST for our application.
 */
public class MongoDateTimeConverters {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    public static final ZoneOffset IST_OFFSET = ZoneOffset.ofHoursMinutes(5, 30);

    // ============================================
    // LocalDateTime Converters
    // ============================================

    @WritingConverter
    public static class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
        @Override
        public Date convert(@NonNull LocalDateTime source) {
            // Treat the LocalDateTime as IST and convert to UTC for storage
            Instant instant = source.atZone(IST).toInstant();
            return Date.from(instant);
        }
    }

    @ReadingConverter
    public static class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
        @Override
        public LocalDateTime convert(@NonNull Date source) {
            // Convert UTC Date to IST LocalDateTime
            return LocalDateTime.ofInstant(source.toInstant(), IST);
        }
    }

    // ============================================
    // ZonedDateTime Converters
    // ============================================

    @WritingConverter
    public static class ZonedDateTimeToDateConverter implements Converter<ZonedDateTime, Date> {
        @Override
        public Date convert(@NonNull ZonedDateTime source) {
            // Convert any timezone to IST first, then store as UTC
            ZonedDateTime istZoned = source.withZoneSameInstant(IST);
            return Date.from(istZoned.toInstant());
        }
    }

    @ReadingConverter
    public static class DateToZonedDateTimeConverter implements Converter<Date, ZonedDateTime> {
        @Override
        public ZonedDateTime convert(@NonNull Date source) {
            // Read UTC Date and return as IST ZonedDateTime
            return ZonedDateTime.ofInstant(source.toInstant(), IST);
        }
    }

    // ============================================
    // OffsetDateTime Converters
    // ============================================

    @WritingConverter
    public static class OffsetDateTimeToDateConverter implements Converter<OffsetDateTime, Date> {
        @Override
        public Date convert(@NonNull OffsetDateTime source) {
            // Convert any offset to IST first, then store as UTC
            ZonedDateTime istZoned = source.atZoneSameInstant(IST);
            return Date.from(istZoned.toInstant());
        }
    }

    @ReadingConverter
    public static class DateToOffsetDateTimeConverter implements Converter<Date, OffsetDateTime> {
        @Override
        public OffsetDateTime convert(@NonNull Date source) {
            // Read UTC Date and return as IST OffsetDateTime
            return OffsetDateTime.ofInstant(source.toInstant(), IST);
        }
    }

    // ============================================
    // Instant Converters (no conversion needed)
    // ============================================

    @WritingConverter
    public static class InstantToDateConverter implements Converter<Instant, Date> {
        @Override
        public Date convert(@NonNull Instant source) {
            return Date.from(source);
        }
    }

    @ReadingConverter
    public static class DateToInstantConverter implements Converter<Date, Instant> {
        @Override
        public Instant convert(@NonNull Date source) {
            return source.toInstant();
        }
    }

    // ============================================
    // LocalDate Converters
    // ============================================

    @WritingConverter
    public static class LocalDateToDateConverter implements Converter<LocalDate, Date> {
        @Override
        public Date convert(@NonNull LocalDate source) {
            // Convert LocalDate to start of day in IST, then to UTC for storage
            ZonedDateTime istZoned = source.atStartOfDay(IST);
            return Date.from(istZoned.toInstant());
        }
    }

    @ReadingConverter
    public static class DateToLocalDateConverter implements Converter<Date, LocalDate> {
        @Override
        public LocalDate convert(@NonNull Date source) {
            // Convert UTC Date to LocalDate in IST
            return LocalDateTime.ofInstant(source.toInstant(), IST).toLocalDate();
        }
    }

    // ============================================
    // LocalTime Converters
    // ============================================

    @WritingConverter
    public static class LocalTimeToLongConverter implements Converter<LocalTime, Long> {
        @Override
        public Long convert(@NonNull LocalTime source) {
            // Store as nanoseconds since midnight
            return source.toNanoOfDay();
        }
    }

    @ReadingConverter
    public static class LongToLocalTimeConverter implements Converter<Long, LocalTime> {
        @Override
        public LocalTime convert(@NonNull Long source) {
            // Read nanoseconds and convert to LocalTime
            return LocalTime.ofNanoOfDay(source);
        }
    }
}
