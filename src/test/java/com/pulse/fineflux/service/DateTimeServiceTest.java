package com.pulse.fineflux.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify IST timezone conversion functionality.
 * Tests date/time conversions from various timezones to IST.
 */
@SpringBootTest
public class DateTimeServiceTest {

    @Autowired
    private DateTimeService dateTimeService;

    @Test
    public void testNowMethodsReturnIstTime() {
        LocalDateTime nowLocal = dateTimeService.nowLocal();
        ZonedDateTime nowZoned = dateTimeService.nowZoned();

        assertNotNull(nowLocal);
        assertNotNull(nowZoned);
        assertEquals(DateTimeService.IST, nowZoned.getZone());
    }

    @Test
    public void testParseIsoToIst_FromUTC() {
        // Test UTC to IST conversion
        // UTC time: 2025-11-20T16:00:00Z
        // IST time: 2025-11-20T21:30:00 (UTC + 5:30)
        String utcDateTime = "2025-11-20T16:00:00Z";

        LocalDateTime result = dateTimeService.parseIsoToIst(utcDateTime);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(11, result.getMonthValue());
        assertEquals(20, result.getDayOfMonth());
        assertEquals(21, result.getHour()); // 16 + 5.5 = 21.5 → 21:30
        assertEquals(30, result.getMinute());
    }

    @Test
    public void testParseIsoToIst_FromUSEastern() {
        // Test US Eastern to IST conversion
        // US Eastern: 2025-11-20T10:30:00-05:00
        // UTC equivalent: 2025-11-20T15:30:00Z
        // IST time: 2025-11-20T21:00:00 (UTC + 5:30)
        String usEasternDateTime = "2025-11-20T10:30:00-05:00";

        LocalDateTime result = dateTimeService.parseIsoToIst(usEasternDateTime);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(11, result.getMonthValue());
        assertEquals(20, result.getDayOfMonth());
        assertEquals(21, result.getHour());
        assertEquals(0, result.getMinute());
    }

    @Test
    public void testParseIsoToIst_FromSingapore() {
        // Test Singapore time to IST conversion
        // Singapore: 2025-11-20T23:30:00+08:00
        // UTC equivalent: 2025-11-20T15:30:00Z
        // IST time: 2025-11-20T21:00:00
        String singaporeDateTime = "2025-11-20T23:30:00+08:00";

        LocalDateTime result = dateTimeService.parseIsoToIst(singaporeDateTime);

        assertNotNull(result);
        assertEquals(21, result.getHour());
        assertEquals(0, result.getMinute());
    }

    @Test
    public void testParseIsoToIst_FromIST() {
        // Test IST to IST (should remain the same)
        String istDateTime = "2025-11-20T21:00:00+05:30";

        LocalDateTime result = dateTimeService.parseIsoToIst(istDateTime);

        assertNotNull(result);
        assertEquals(21, result.getHour());
        assertEquals(0, result.getMinute());
    }

    @Test
    public void testToIstFromZonedDateTime() {
        // Create a ZonedDateTime in US Pacific timezone
        ZonedDateTime pacificTime = ZonedDateTime.of(
            2025, 11, 20, 8, 0, 0, 0,
            ZoneId.of("America/Los_Angeles")
        );

        LocalDateTime istTime = dateTimeService.toIstFromZonedDateTime(pacificTime);

        assertNotNull(istTime);
        // Pacific (UTC-8) to UTC: +8 hours = 16:00 UTC
        // UTC to IST: +5:30 = 21:30 IST
        assertEquals(21, istTime.getHour());
        assertEquals(30, istTime.getMinute());
    }

    @Test
    public void testToIstFromOffsetDateTime() {
        // Create an OffsetDateTime in London timezone (UTC+0)
        OffsetDateTime londonTime = OffsetDateTime.of(
            2025, 11, 20, 16, 0, 0, 0,
            ZoneOffset.UTC
        );

        LocalDateTime istTime = dateTimeService.toIstFromOffsetDateTime(londonTime);

        assertNotNull(istTime);
        assertEquals(21, istTime.getHour()); // 16:00 + 5:30 = 21:30
        assertEquals(30, istTime.getMinute());
    }

    @Test
    public void testToIstFromInstant() {
        // Create an Instant (always UTC)
        Instant instant = Instant.parse("2025-11-20T16:00:00Z");

        LocalDateTime istTime = dateTimeService.toIstFromInstant(instant);

        assertNotNull(istTime);
        assertEquals(21, istTime.getHour());
        assertEquals(30, istTime.getMinute());
    }

    @Test
    public void testToInstant() {
        LocalDateTime istTime = LocalDateTime.of(2025, 11, 20, 21, 30, 0);

        Instant instant = dateTimeService.toInstant(istTime);

        assertNotNull(instant);
        // IST 21:30 = UTC 16:00
        assertEquals("2025-11-20T16:00:00Z", instant.toString());
    }

    @Test
    public void testGetStartOfDay() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 11, 20, 15, 30, 45);

        LocalDateTime startOfDay = dateTimeService.getStartOfDay(dateTime);

        assertNotNull(startOfDay);
        assertEquals(0, startOfDay.getHour());
        assertEquals(0, startOfDay.getMinute());
        assertEquals(0, startOfDay.getSecond());
    }

    @Test
    public void testGetEndOfDay() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 11, 20, 10, 30, 45);

        LocalDateTime endOfDay = dateTimeService.getEndOfDay(dateTime);

        assertNotNull(endOfDay);
        assertEquals(23, endOfDay.getHour());
        assertEquals(59, endOfDay.getMinute());
        assertEquals(59, endOfDay.getSecond());
    }

    @Test
    public void testIsToday() {
        LocalDateTime now = dateTimeService.nowLocal();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);

        assertTrue(dateTimeService.isToday(now));
        assertFalse(dateTimeService.isToday(yesterday));
        assertFalse(dateTimeService.isToday(tomorrow));
    }

    @Test
    public void testDaysBetween() {
        LocalDateTime start = LocalDateTime.of(2025, 11, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 11, 25, 15, 0);

        long days = dateTimeService.daysBetween(start, end);

        assertEquals(5, days);
    }

    @Test
    public void testParseFlexible_IsoFormat() {
        String isoDate = "2025-11-20T10:30:00Z";

        LocalDateTime result = dateTimeService.parseFlexible(isoDate);

        assertNotNull(result);
    }

    @Test
    public void testParseFlexible_DisplayFormat() {
        String displayDate = "2025-11-20 10:30:00";

        LocalDateTime result = dateTimeService.parseFlexible(displayDate);

        assertNotNull(result);
        assertEquals(10, result.getHour());
        assertEquals(30, result.getMinute());
    }

    @Test
    public void testParseFlexible_DateOnlyFormat() {
        String dateOnly = "2025-11-20";

        LocalDateTime result = dateTimeService.parseFlexible(dateOnly);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(11, result.getMonthValue());
        assertEquals(20, result.getDayOfMonth());
        assertEquals(0, result.getHour()); // Start of day
    }

    @Test
    public void testToIsoString() {
        LocalDateTime istTime = LocalDateTime.of(2025, 11, 20, 21, 30, 0);

        String isoString = dateTimeService.toIsoString(istTime);

        assertNotNull(isoString);
        assertTrue(isoString.contains("2025-11-20"));
        assertTrue(isoString.contains("21:30"));
    }

    @Test
    public void testToDisplayString() {
        LocalDateTime istTime = LocalDateTime.of(2025, 11, 20, 21, 30, 45);

        String displayString = dateTimeService.toDisplayString(istTime);

        assertNotNull(displayString);
        assertEquals("2025-11-20 21:30:45", displayString);
    }

    @Test
    public void testNullHandling() {
        assertNull(dateTimeService.toIstFromZonedDateTime(null));
        assertNull(dateTimeService.toIstFromOffsetDateTime(null));
        assertNull(dateTimeService.toIstFromInstant(null));
        assertNull(dateTimeService.toInstant(null));
        assertNull(dateTimeService.getStartOfDay(null));
        assertNull(dateTimeService.getEndOfDay(null));
        assertNull(dateTimeService.toIsoString(null));
        assertNull(dateTimeService.toDisplayString(null));
        assertFalse(dateTimeService.isToday(null));
        assertEquals(0, dateTimeService.daysBetween(null, null));
    }

    @Test
    public void testTimezoneConversionAccuracy() {
        // Comprehensive timezone conversion test
        // Create same instant in different timezones and ensure all convert to same IST time
        Instant instant = Instant.parse("2025-11-20T16:00:00Z");

        ZonedDateTime utc = instant.atZone(ZoneId.of("UTC"));
        ZonedDateTime tokyo = instant.atZone(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime newYork = instant.atZone(ZoneId.of("America/New_York"));
        ZonedDateTime london = instant.atZone(ZoneId.of("Europe/London"));

        LocalDateTime istFromUtc = dateTimeService.toIstFromZonedDateTime(utc);
        LocalDateTime istFromTokyo = dateTimeService.toIstFromZonedDateTime(tokyo);
        LocalDateTime istFromNewYork = dateTimeService.toIstFromZonedDateTime(newYork);
        LocalDateTime istFromLondon = dateTimeService.toIstFromZonedDateTime(london);

        // All should convert to the same IST time
        assertEquals(istFromUtc, istFromTokyo);
        assertEquals(istFromUtc, istFromNewYork);
        assertEquals(istFromUtc, istFromLondon);

        // Verify it's correct IST time (UTC 16:00 + 5:30 = IST 21:30)
        assertEquals(21, istFromUtc.getHour());
        assertEquals(30, istFromUtc.getMinute());
    }
}

