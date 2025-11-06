package com.pulse.fineflux.utill;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class SaleMatch {
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter SECOND_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private SaleMatch(){}


    public static LocalDateTime toIstSecondPlus50(LocalDateTime source, ZoneId sourceZone) {
        if (source == null) return null;
        ZonedDateTime istZdt = source.atZone(sourceZone).withZoneSameInstant(IST); // convert to IST keeping instant [web:23][web:30]
        LocalDateTime istLdt = istZdt.toLocalDateTime().plusSeconds(5); // add 50 seconds [web:41][web:43]
        return istLdt.truncatedTo(ChronoUnit.SECONDS); // drop nanos to second precision [web:35][web:39]
    }
    public static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    public static String priceBucket(double price) {
        // Keep exact string with fixed scale to avoid FP surprises in key
        return String.format("%.2f", price);
    }

    public static String buildKey(LocalDateTime istSecond, String productNorm, String gunsNorm, double price) {
        String ts = SECOND_FMT.format(istSecond);
        return ts + "|" + productNorm + "|" + gunsNorm + "|" + priceBucket(price);
    }
}

