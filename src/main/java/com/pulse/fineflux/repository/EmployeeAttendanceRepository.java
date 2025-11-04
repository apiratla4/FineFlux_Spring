package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.EmployeeAttendance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface EmployeeAttendanceRepository extends MongoRepository<EmployeeAttendance, String> {

    // Basic CRUD is handled by MongoRepository

    // Find all by employee id
    List<EmployeeAttendance> findByEmpId(String empId);

    // Find by attendance id (default from MongoRepository, optional: findById)
    // Optional: findByOrganizationIdAndId if needed for multi-tenant/org data

    // Day query: get attendance records for one specific day
    @Query("{'empId': ?0, 'checkIn': { $gte: ?1, $lt: ?2 }}")
    List<EmployeeAttendance> findByEmpIdAndCheckInBetween(String empId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    default List<EmployeeAttendance> findOneDay(String empId, LocalDateTime date) {
        LocalDateTime start = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusDays(1);
        return findByEmpIdAndCheckInBetween(empId, start, end);
    }

    // Week query: get attendance records for a week starting from reference date
    default List<EmployeeAttendance> findOneWeek(String empId, LocalDateTime reference) {
        LocalDateTime start = reference.withHour(0).withMinute(0).withSecond(0).withNano(0)
                .minusDays(reference.getDayOfWeek().getValue() - 1);
        LocalDateTime end = start.plusWeeks(1);
        return findByEmpIdAndCheckInBetween(empId, start, end);
    }

    // Month query: get attendance records for a month
    default List<EmployeeAttendance> findOneMonth(String empId, LocalDateTime reference) {
        LocalDateTime start = reference.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusMonths(1);
        return findByEmpIdAndCheckInBetween(empId, start, end);
    }

    // Three months query
    default List<EmployeeAttendance> findThreeMonths(String empId, LocalDateTime reference) {
        LocalDateTime start = reference.minusMonths(2).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusMonths(3);
        return findByEmpIdAndCheckInBetween(empId, start, end);
    }

    // Six months query
    default List<EmployeeAttendance> findSixMonths(String empId, LocalDateTime reference) {
        LocalDateTime start = reference.minusMonths(5).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusMonths(6);
        return findByEmpIdAndCheckInBetween(empId, start, end);
    }
}
