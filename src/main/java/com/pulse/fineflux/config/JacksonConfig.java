package com.pulse.fineflux.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Jackson configuration for JSON serialization/deserialization with IST timezone.
 * Ensures all date/time values in REST API requests/responses use IST.
 */
@Configuration
public class JacksonConfig {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder
                .timeZone(TimeZone.getTimeZone(IST))
                .build();

        // Configure JavaTimeModule with IST timezone
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // Configure serializers and deserializers to use IST
        objectMapper.registerModule(javaTimeModule);

        // Configure ObjectMapper features
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);

        // Set default timezone to IST
        objectMapper.setTimeZone(TimeZone.getTimeZone(IST));

        return objectMapper;
    }

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .timeZone(TimeZone.getTimeZone(IST))
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    }
}
