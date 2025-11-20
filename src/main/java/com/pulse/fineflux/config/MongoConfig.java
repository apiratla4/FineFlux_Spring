package com.pulse.fineflux.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.TimeZone;

/**
 * MongoDB configuration with custom converters for IST timezone standardization.
 * All date/time values will be stored in IST (Asia/Kolkata) timezone in MongoDB.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.pulse.fineflux.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        return MongoClients.create(mongoClientSettings);
    }

    /**
     * Register custom MongoDB converters for timezone-aware date/time handling.
     * All dates will be converted to IST before storage and from IST when reading.
     */
    @Bean
    @Override
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                // LocalDateTime converters
                new MongoDateTimeConverters.LocalDateTimeToDateConverter(),
                new MongoDateTimeConverters.DateToLocalDateTimeConverter(),

                // ZonedDateTime converters
                new MongoDateTimeConverters.ZonedDateTimeToDateConverter(),
                new MongoDateTimeConverters.DateToZonedDateTimeConverter(),

                // OffsetDateTime converters
                new MongoDateTimeConverters.OffsetDateTimeToDateConverter(),
                new MongoDateTimeConverters.DateToOffsetDateTimeConverter(),

                // Instant converters
                new MongoDateTimeConverters.InstantToDateConverter(),
                new MongoDateTimeConverters.DateToInstantConverter(),

                // LocalDate converters
                new MongoDateTimeConverters.LocalDateToDateConverter(),
                new MongoDateTimeConverters.DateToLocalDateConverter(),

                // LocalTime converters
                new MongoDateTimeConverters.LocalTimeToLongConverter(),
                new MongoDateTimeConverters.LongToLocalTimeConverter()
        ));
    }

    /**
     * Set default JVM timezone to IST.
     * This ensures consistency across the entire application.
     */
    static {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Kolkata")));
        System.setProperty("user.timezone", "Asia/Kolkata");
    }
}

