# IST Timezone Standardization for MongoDB - Implementation Guide

## Overview
This implementation ensures all date/time data is stored in **Indian Standard Time (IST - Asia/Kolkata)** in MongoDB, regardless of the user's timezone or the server's location.

## Components Implemented

### 1. MongoDateTimeConverters.java
**Location**: `com.pulse.fineflux.config.MongoDateTimeConverters`

Custom MongoDB converters that handle automatic conversion of date/time types to IST:

#### Reading Converters (From MongoDB to Java)
- `DateToLocalDateTimeConverter` - Converts MongoDB Date to LocalDateTime in IST
- `DateToZonedDateTimeConverter` - Converts MongoDB Date to ZonedDateTime in IST
- `DateToOffsetDateTimeConverter` - Converts MongoDB Date to OffsetDateTime in IST
- `DateToInstantConverter` - Converts MongoDB Date to Instant
- `DateToLocalDateConverter` - Converts MongoDB Date to LocalDate in IST
- `LongToLocalTimeConverter` - Converts Long to LocalTime

#### Writing Converters (From Java to MongoDB)
- `LocalDateTimeToDateConverter` - Converts LocalDateTime to MongoDB Date (stored as IST)
- `ZonedDateTimeToDateConverter` - Converts ZonedDateTime to MongoDB Date (converts to IST first)
- `OffsetDateTimeToDateConverter` - Converts OffsetDateTime to MongoDB Date (converts to IST first)
- `InstantToDateConverter` - Converts Instant to MongoDB Date
- `LocalDateToDateConverter` - Converts LocalDate to MongoDB Date (start of day in IST)
- `LocalTimeToLongConverter` - Converts LocalTime to Long (nanoseconds)

### 2. MongoConfig.java
**Location**: `com.pulse.fineflux.config.MongoConfig`

Configuration class that:
- Registers all custom MongoDB converters
- Configures MongoDB client settings
- Sets JVM default timezone to IST in a static initializer block
- Enables MongoDB repositories

### 3. JacksonConfig.java
**Location**: `com.pulse.fineflux.config.JacksonConfig`

Jackson JSON configuration for REST API serialization/deserialization:
- Configures ObjectMapper to use IST timezone
- Disables timestamp serialization (uses ISO-8601 format)
- Prevents automatic timezone adjustment for incoming dates
- Ensures consistent JSON date handling

### 4. DateTimeService.java
**Location**: `com.pulse.fineflux.service.DateTimeService`

Service class providing utility methods for date/time operations:
- `nowLocal()` - Get current time in IST as LocalDateTime
- `nowZoned()` - Get current time in IST as ZonedDateTime
- `nowInstant()` - Get current time as Instant
- `parseIsoToIst(String)` - Parse ISO-8601 string from frontend and convert to IST
- `toIstFromZonedDateTime(ZonedDateTime)` - Convert any timezone to IST
- `toIstFromOffsetDateTime(OffsetDateTime)` - Convert any timezone to IST
- `toIstFromInstant(Instant)` - Convert Instant to IST LocalDateTime
- Various formatting and calculation methods

### 5. Application Configuration Updates

#### application.properties
Added MongoDB and Jackson timezone settings:
```properties
spring.jackson.time-zone=Asia/Kolkata
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.deserialization.adjust-dates-to-context-time-zone=false
```

#### FinefluxApplication.java
Added timezone initialization at application startup:
```java
TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Kolkata")));
System.setProperty("user.timezone", "Asia/Kolkata");
```

## How It Works

### Data Flow - Writing to MongoDB

1. **Frontend Sends Date**: User submits date from any timezone (e.g., "2025-11-20T10:30:00-05:00" - US Eastern)
2. **Jackson Deserializes**: JSON is parsed by Jackson, which maintains timezone information
3. **Controller Receives**: Controller receives ZonedDateTime/OffsetDateTime/LocalDateTime
4. **Service Layer**: Can use DateTimeService to explicitly convert if needed
5. **MongoDB Converter**: Before saving, the appropriate WritingConverter automatically converts to IST
6. **Storage**: Date is stored in MongoDB as Date object representing IST time

### Data Flow - Reading from MongoDB

1. **MongoDB Query**: Application queries for documents with date fields
2. **MongoDB Converter**: ReadingConverter automatically converts Date to Java type in IST
3. **Service Returns**: Data returned with dates in IST
4. **Jackson Serializes**: Dates serialized to JSON with IST timezone
5. **Frontend Receives**: Frontend receives ISO-8601 formatted dates with IST timezone info

## Usage Examples

### Example 1: Using in Entity Classes

```java
@Document(collection = "sales")
public class Sales {
    @Id
    private String id;
    
    // Will be automatically converted to IST by MongoDB converters
    private LocalDateTime dateTime;
    
    // Also supported
    private ZonedDateTime transactionTime;
    private OffsetDateTime createdAt;
    private Instant updatedAt;
}
```

### Example 2: Using in Controller

```java
@RestController
@RequestMapping("/api/sales")
public class SalesController {
    
    @Autowired
    private DateTimeService dateTimeService;
    
    @PostMapping
    public ResponseEntity<Sales> createSale(@RequestBody SaleRequest request) {
        Sales sale = new Sales();
        
        // If frontend sends ISO string with timezone
        LocalDateTime istDateTime = dateTimeService.parseIsoToIst(request.getDateTime());
        sale.setDateTime(istDateTime);
        
        // Or simply use current time in IST
        sale.setCreatedAt(dateTimeService.nowZoned());
        
        // MongoDB converters will ensure it's stored in IST
        salesRepository.save(sale);
        
        return ResponseEntity.ok(sale);
    }
}
```

### Example 3: Handling Frontend Dates with Timezone

```java
// Frontend sends: "2025-11-20T10:30:00-05:00" (US Eastern Time)
String frontendDateTime = "2025-11-20T10:30:00-05:00";

// Convert to IST
LocalDateTime istDateTime = dateTimeService.parseIsoToIst(frontendDateTime);
// Result: 2025-11-20T21:00:00 (IST = Eastern + 10:30 hours)

// Save to MongoDB (will be stored as IST)
entity.setDateTime(istDateTime);
repository.save(entity);
```

### Example 4: Querying by Date Range

```java
@Service
public class SalesService {
    
    @Autowired
    private DateTimeService dateTimeService;
    
    public List<Sales> getSalesByDateRange(String startDateStr, String endDateStr) {
        // Parse frontend dates (converts to IST automatically)
        LocalDateTime startDate = dateTimeService.parseIsoToIst(startDateStr);
        LocalDateTime endDate = dateTimeService.parseIsoToIst(endDateStr);
        
        // Get start and end of day in IST
        LocalDateTime start = dateTimeService.getStartOfDay(startDate);
        LocalDateTime end = dateTimeService.getEndOfDay(endDate);
        
        // Query MongoDB (dates are in IST)
        return salesRepository.findByDateTimeBetween(start, end);
    }
}
```

## Migration Strategy for Existing Data

If you have existing data in MongoDB with inconsistent timezones:

### Option 1: One-time Migration Script

```java
@Service
public class DateMigrationService {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private DateTimeService dateTimeService;
    
    public void migrateDatesTtoIST() {
        // Get all documents
        List<Sales> allSales = mongoTemplate.findAll(Sales.class);
        
        for (Sales sale : allSales) {
            // If you know the original timezone, convert it
            // Assuming dates were stored in UTC
            if (sale.getDateTime() != null) {
                ZonedDateTime utcTime = sale.getDateTime()
                    .atZone(ZoneId.of("UTC"));
                LocalDateTime istTime = dateTimeService
                    .toIstFromZonedDateTime(utcTime);
                sale.setDateTime(istTime);
            }
            mongoTemplate.save(sale);
        }
    }
}
```

### Option 2: Gradual Migration

The converters will handle new data correctly. Existing data will be read and interpreted as IST going forward.

## Testing

### Test Date Conversion

```java
@SpringBootTest
public class DateTimeConversionTest {
    
    @Autowired
    private DateTimeService dateTimeService;
    
    @Test
    public void testFrontendDateConversionToIST() {
        // Test various timezone inputs
        String usEastern = "2025-11-20T10:30:00-05:00";
        String utc = "2025-11-20T16:00:00Z";
        String singapore = "2025-11-20T23:30:00+08:00";
        
        LocalDateTime istFromUS = dateTimeService.parseIsoToIst(usEastern);
        LocalDateTime istFromUTC = dateTimeService.parseIsoToIst(utc);
        LocalDateTime istFromSG = dateTimeService.parseIsoToIst(singapore);
        
        // All should convert to IST (UTC+5:30)
        // US Eastern (UTC-5) + 10:30 = 21:00 IST
        assertEquals(LocalDateTime.of(2025, 11, 20, 21, 0), istFromUS);
        
        // UTC + 5:30 = 21:30 IST
        assertEquals(LocalDateTime.of(2025, 11, 20, 21, 30), istFromUTC);
        
        // Singapore (UTC+8) - 2:30 = 21:00 IST
        assertEquals(LocalDateTime.of(2025, 11, 20, 21, 0), istFromSG);
    }
}
```

## Benefits

1. **Consistency**: All dates stored in a single timezone (IST)
2. **Simplicity**: No need to handle timezone conversions in business logic
3. **Accuracy**: No data loss or confusion from mixed timezones
4. **Automatic**: Converters handle everything transparently
5. **Frontend Agnostic**: Works with dates from any timezone
6. **Query Friendly**: Date range queries work correctly since all dates are in IST

## Important Notes

1. **LocalDateTime**: When using LocalDateTime, it's assumed to be in IST after conversion
2. **Frontend Coordination**: Frontend should send ISO-8601 formatted dates with timezone info
3. **Display**: When showing dates to users, you may want to convert back to their local timezone in the frontend
4. **Instant vs LocalDateTime**: 
   - Use `Instant` for absolute points in time (independent of timezone)
   - Use `LocalDateTime` for dates in IST context
   - Use `ZonedDateTime` when you need to preserve timezone information

## Troubleshooting

### Issue: Dates still showing wrong timezone
**Solution**: Ensure the application has been restarted after adding the configuration

### Issue: Existing data shows incorrect times
**Solution**: Run the migration script to convert existing data to IST

### Issue: Frontend dates not converting properly
**Solution**: Ensure frontend sends ISO-8601 formatted dates with timezone information

### Issue: Jackson not respecting timezone
**Solution**: Verify JacksonConfig is loaded and ObjectMapper is using IST timezone

## Additional Resources

- [Java Time API Documentation](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html)
- [Spring Data MongoDB Converters](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.converters)
- [Jackson Date/Time Handling](https://github.com/FasterXML/jackson-modules-java8)

## Support

For issues or questions, please contact the development team or refer to the project documentation.

