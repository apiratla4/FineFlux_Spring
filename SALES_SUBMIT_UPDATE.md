# Sales Submit - Manual Date/Time Selection Update

## Overview
The sales submission endpoint has been updated to support **manual date/time selection** instead of auto-updating the date and time. You can now specify the exact date and time for each sales entry via the API payload.

## Changes Made

### 1. SalesCreateDTO
- Added detailed JavaDoc documentation to the `dateTime` field
- The `dateTime` field now explicitly explains:
  - If provided in the payload, that value will be used
  - If null, the system will use the current IST time automatically
  - This allows manual date/time selection instead of auto-updating

### 2. SalesServiceImpl
- Updated the comment to clarify the behavior:
  - When `dto.getDateTime()` is provided (not null), it uses that date/time
  - When `dto.getDateTime()` is null, it defaults to current IST time
- Implementation already supported this functionality

## How to Use

### Request Example - With Manual Date/Time

```json
{
  "organizationId": "org-123",
  "empId": "emp-456",
  "productName": "Premium Oil",
  "openingStock": 100.0,
  "closingStock": 85.5,
  "testingTotal": 2.0,
  "salesInLiters": 12.5,
  "price": 500.0,
  "salesInRupees": 6250.0,
  "dateTime": "2026-04-20T14:30:00",
  "guns": "Gun-001"
}
```

### Request Example - Auto Date/Time (Legacy)

```json
{
  "organizationId": "org-123",
  "empId": "emp-456",
  "productName": "Premium Oil",
  "openingStock": 100.0,
  "closingStock": 85.5,
  "testingTotal": 2.0,
  "salesInLiters": 12.5,
  "price": 500.0,
  "salesInRupees": 6250.0,
  "dateTime": null,
  "guns": "Gun-001"
}
```

## API Endpoint

```
POST /api/organizations/{orgId}/sales
Content-Type: application/json
```

## Behavior

| Scenario | Behavior |
|----------|----------|
| `dateTime` provided in payload | Uses the provided date/time (IST timezone) |
| `dateTime` is null | Uses current IST time automatically |
| `dateTime` omitted from payload | Uses current IST time automatically |

## Timezone
- All date/time values are stored and processed in **IST (Indian Standard Time)** timezone
- Ensure your frontend sends date/time in ISO 8601 format with appropriate timezone conversion

## Notes

1. **InventoryLog**: The inventory log timestamp (`lastUpdated`) still uses the current time when the inventory is updated, which is the correct behavior for audit trails.

2. **Backward Compatibility**: This change is fully backward compatible. Existing code that doesn't send `dateTime` will continue to work with auto-updating behavior.

3. **Validation**: The `dateTime` field is optional and not validated as mandatory, giving flexibility in how you use the API.

## Related Files Updated

- `src/main/java/com/pulse/fineflux/domain/SalesCreateDTO.java`
- `src/main/java/com/pulse/fineflux/service/SalesServiceImpl.java`

