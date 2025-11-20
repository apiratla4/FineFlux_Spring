# IST Timezone - Quick Reference Guide

## Quick Start

### 1. Using Dates in Controllers

```java
@RestController
@RequestMapping("/api/sales")
public class SalesController {
    
    @Autowired
    private DateTimeService dateTimeService;
    
    @PostMapping
    public ResponseEntity<Sales> createSale(@RequestBody SaleRequest request) {
        Sales sale = new Sales();
        
        // Option 1: If frontend sends ISO string with timezone
        LocalDateTime istDateTime = dateTimeService.parseIsoToIst(request.getDateTime());
        sale.setDateTime(istDateTime);
        
        // Option 2: Use current IST time
        sale.setDateTime(dateTimeService.nowLocal());
        
        // MongoDB converters will automatically store in IST
        return ResponseEntity.ok(salesRepository.save(sale));
    }
}
```

### 2. Using Dates in Entities

```java
@Document(collection = "sales")
public class Sales {
    @Id
    private String id;
    
    // Use LocalDateTime for dates in IST context
    private LocalDateTime dateTime;
    
    // Use Instant for absolute timestamps
    private Instant createdAt;
    
    // Use ZonedDateTime if you need to preserve timezone info
    private ZonedDateTime transactionTime;
}
```

### 3. Date Range Queries

```java
@Service
public class SalesService {
    
    @Autowired
    private DateTimeService dateTimeService;
    
    @Autowired
    private SalesRepository salesRepository;
    
    public List<Sales> getSalesForToday() {
        LocalDateTime now = dateTimeService.nowLocal();
        LocalDateTime startOfDay = dateTimeService.getStartOfDay(now);
        LocalDateTime endOfDay = dateTimeService.getEndOfDay(now);
        
        return salesRepository.findByDateTimeBetween(startOfDay, endOfDay);
    }
}
```

### 4. Frontend Integration

#### Frontend sends (any timezone):
```json
{
  "dateTime": "2025-11-20T10:30:00-05:00",
  "amount": 1000.00
}
```

#### Backend automatically converts to IST and stores:
```json
{
  "_id": "123",
  "dateTime": ISODate("2025-11-20T16:00:00.000Z"),  // Stored as UTC in MongoDB
  "amount": 1000.00
}
```

#### Backend returns to frontend:
```json
{
  "id": "123",
  "dateTime": "2025-11-20T21:30:00+05:30",  // IST timezone
  "amount": 1000.00
}
```

## Common Scenarios

### Scenario 1: Accept Date from Frontend (Any Timezone)

```java
@PostMapping("/create")
public ResponseEntity<?> create(@RequestBody CreateRequest request) {
    // Frontend sends: "2025-11-20T10:30:00-05:00" (US Eastern)
    LocalDateTime istTime = dateTimeService.parseIsoToIst(request.getDateTime());
    // Result: 2025-11-20T21:00:00 (IST)
    
    entity.setDateTime(istTime);
    repository.save(entity);  // Stored as IST in MongoDB
    
    return ResponseEntity.ok(entity);
}
```

### Scenario 2: Get Current Time in IST

```java
@PostMapping("/record")
public ResponseEntity<?> recordTransaction() {
    Transaction transaction = new Transaction();
    transaction.setTimestamp(dateTimeService.nowLocal());  // IST
    return ResponseEntity.ok(repository.save(transaction));
}
```

### Scenario 3: Query by Date Range

```java
@GetMapping("/reports")
public ResponseEntity<?> getReport(
    @RequestParam String startDate,
    @RequestParam String endDate) {
    
    // Parse dates from frontend (converts to IST)
    LocalDateTime start = dateTimeService.parseIsoToIst(startDate);
    LocalDateTime end = dateTimeService.parseIsoToIst(endDate);
    
    // Query MongoDB (all dates are in IST)
    List<Record> records = repository.findByDateTimeBetween(start, end);
    
    return ResponseEntity.ok(records);
}
```

### Scenario 4: Calculate Days Between Dates

```java
@GetMapping("/duration")
public ResponseEntity<?> calculateDuration(@RequestParam String from, @RequestParam String to) {
    LocalDateTime startDate = dateTimeService.parseFlexible(from);
    LocalDateTime endDate = dateTimeService.parseFlexible(to);
    
    long days = dateTimeService.daysBetween(startDate, endDate);
    
    return ResponseEntity.ok(Map.of("days", days));
}
```

## Repository Methods

```java
public interface SalesRepository extends MongoRepository<Sales, String> {
    
    // Find by exact date
    List<Sales> findByDateTime(LocalDateTime dateTime);
    
    // Find by date range
    List<Sales> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
    
    // Find after a date
    List<Sales> findByDateTimeAfter(LocalDateTime date);
    
    // Find before a date
    List<Sales> findByDateTimeBefore(LocalDateTime date);
    
    // Custom query
    @Query("{ 'dateTime': { $gte: ?0, $lte: ?1 } }")
    List<Sales> findInDateRange(LocalDateTime start, LocalDateTime end);
}
```

## DTO Examples

```java
@Data
public class SaleRequest {
    
    // Frontend sends ISO-8601 with timezone
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String dateTime;  // "2025-11-20T10:30:00-05:00"
    
    private Double amount;
    private String productName;
}

@Data
public class SaleResponse {
    
    private String id;
    
    // Returns as ISO-8601 with IST timezone
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime dateTime;  // "2025-11-20T21:00:00+05:30"
    
    private Double amount;
    private String productName;
}
```

## Best Practices

### ✅ DO:
- Use `dateTimeService.nowLocal()` for current time
- Use `dateTimeService.parseIsoToIst()` for frontend dates
- Use `LocalDateTime` for dates in IST context
- Use `Instant` for absolute timestamps
- Let MongoDB converters handle the conversion automatically

### ❌ DON'T:
- Don't use `LocalDateTime.now()` directly (uses system timezone)
- Don't use `new Date()` (uses system timezone)
- Don't manually convert timezones in business logic
- Don't store dates as strings in MongoDB
- Don't use deprecated `java.util.Date` unless necessary

## Troubleshooting

### Problem: Dates showing wrong time
**Solution**: Ensure you're using `dateTimeService.nowLocal()` instead of `LocalDateTime.now()`

### Problem: Frontend dates not converting
**Solution**: Frontend must send ISO-8601 format with timezone: `"2025-11-20T10:30:00-05:00"`

### Problem: Queries returning wrong results
**Solution**: Ensure query parameters are parsed using `dateTimeService.parseIsoToIst()`

## Testing Your Implementation

Run the test suite:
```bash
mvn test -Dtest=DateTimeServiceTest
```

All tests should pass, confirming:
- UTC to IST conversion
- Multiple timezone conversions
- Date range calculations
- Null handling
- Format parsing

## Configuration Files

All configuration is already set up:
- ✅ `MongoConfig.java` - Registers converters
- ✅ `MongoDateTimeConverters.java` - Handles conversions
- ✅ `JacksonConfig.java` - JSON serialization
- ✅ `DateTimeService.java` - Utility methods
- ✅ `application.properties` - Spring configuration
- ✅ `FinefluxApplication.java` - Sets default timezone

## Need Help?

Refer to the comprehensive documentation: `IST_TIMEZONE_IMPLEMENTATION.md`

