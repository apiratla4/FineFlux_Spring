package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.EmployeeAttendanceCreateDTO;
import com.pulse.fineflux.domain.EmployeeAttendanceResponseDTO;
import com.pulse.fineflux.domain.EmployeeAttendanceUpdateDTO;
import com.pulse.fineflux.entity.EmployeeAttendance;
import com.pulse.fineflux.entity.EmployeeDuty;
import com.pulse.fineflux.repository.EmployeeAttendanceRepository;
import com.pulse.fineflux.repository.EmployeeDutyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeAttendanceServiceImpl implements EmployeeAttendanceService {
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final EmployeeAttendanceRepository repository;
    private final EmployeeDutyRepository dutyRepository;

    @Autowired
    public EmployeeAttendanceServiceImpl(EmployeeAttendanceRepository repository,
                                         EmployeeDutyRepository dutyRepository) {
        this.repository = repository;
        this.dutyRepository = dutyRepository;
    }

    private LocalDateTime toIST(LocalDateTime input) {
        if (input == null) return null;
        return input.atZone(ZoneId.systemDefault()).withZoneSameInstant(IST_ZONE).toLocalDateTime();
    }

    private String minutesToHourMinFormat(Long minutes) {
        if (minutes == null) return null;
        long abs = Math.abs(minutes);
        long hours = abs / 60;
        long mins = abs % 60;
        String prefix = minutes < 0 ? "-" : "";
        return String.format("%s%02d:%02d", prefix, hours, mins);
    }

    private long calculateBreakMins(LocalDateTime breakIn, LocalDateTime breakOut) {
        if (breakIn == null || breakOut == null) return 0;
        return breakOut.isAfter(breakIn)
                ? Duration.between(breakIn, breakOut).toMinutes()
                : Duration.between(breakIn, breakOut.plusDays(1)).toMinutes();
    }

    private long calculateWorkMins(LocalDateTime checkIn, LocalDateTime checkOut, long breakMins) {
        if (checkIn == null || checkOut == null) return 0;
        long total;
        if (checkOut.isAfter(checkIn)) {
            total = Duration.between(checkIn, checkOut).toMinutes();
        } else {
            total = Duration.between(checkIn, checkOut.plusDays(1)).toMinutes();
        }
        return Math.max(0, total - breakMins);
    }

    @Override
    public EmployeeAttendanceResponseDTO create(EmployeeAttendanceCreateDTO dto) {
        EmployeeAttendance ea = new EmployeeAttendance();

        ea.setOrganizationId(dto.getOrganizationId());
        ea.setEmpId(dto.getEmpId());
        ea.setUsername(dto.getUsername());
        ea.setCheckIn(toIST(dto.getCheckIn()));
        ea.setCheckOut(toIST(dto.getCheckOut()));
        ea.setBreakIn(toIST(dto.getBreakIn()));
        ea.setBreakOut(toIST(dto.getBreakOut()));
        ea.setDescription(dto.getDescription());
        ea.setCreatedAt(LocalDateTime.now(IST_ZONE));
        ea.setUpdatedAt(LocalDateTime.now(IST_ZONE));

        boolean present = ea.getCheckIn() != null && ea.getCheckOut() != null;
        ea.setPresent(present ? "YES" : "NO");
        ea.setAbsent(present ? "NO" : "YES");

        // Duty lookup by empId only; use most recent valid duty
        long dutyMinutes = 0;
        EmployeeDuty duty = (ea.getEmpId() != null)
                ? dutyRepository.findTopByEmpIdAndShiftStartNotNullAndShiftEndNotNullOrderByDutyDateDesc(ea.getEmpId())
                : null;
        if (duty != null) {
            try {
                LocalTime start = LocalTime.parse(duty.getShiftStart());
                LocalTime end = LocalTime.parse(duty.getShiftEnd());
                dutyMinutes = (end.isAfter(start) || end.equals(start))
                        ? Duration.between(start, end).toMinutes()
                        : Duration.between(start, LocalTime.MAX).toMinutes()
                        + Duration.between(LocalTime.MIN, end).toMinutes() + 1;
            } catch (Exception ex) {
                dutyMinutes = 0;
            }
        }
        ea.setActuallyWorkingHoursMins(dutyMinutes);

        long breakMinutes = calculateBreakMins(ea.getBreakIn(), ea.getBreakOut());
        ea.setBreakTimeMins(breakMinutes);

        long actualWorkMins = calculateWorkMins(ea.getCheckIn(), ea.getCheckOut(), breakMinutes);
        ea.setWorkingMins(actualWorkMins);

        long shortTime = 0, extraHours = 0;
        if (actualWorkMins >= dutyMinutes) {
            extraHours = actualWorkMins - dutyMinutes;
            shortTime = 0;
        } else {
            shortTime = dutyMinutes - actualWorkMins;
            extraHours = 0;
        }
        ea.setShortTimeMins(shortTime);
        ea.setExtraHoursMins(extraHours);
        ea.setDescription(
                "Break time = breakOut - breakIn. Duty = scheduled shift mins. Working = checkOut - checkIn - break. "
                        + "Short time = max(0, duty-working). Extra hours = max(0, working-duty)."
        );

        // MONTHLY METRICS (attendanceRate and avgHours)
        double attendanceRate = 0.0;
        double avgHours = 0.0;
        if (ea.getEmpId() != null && ea.getCheckIn() != null) {
            LocalDate checkInDate = ea.getCheckIn().toLocalDate();
            LocalDate firstOfMonth = checkInDate.withDayOfMonth(1);
            LocalDate lastOfMonth = firstOfMonth.plusMonths(1).minusDays(1);

            LocalDateTime startMonth = firstOfMonth.atStartOfDay();
            LocalDateTime endMonth = lastOfMonth.atTime(23,59,59);

            List<EmployeeAttendance> monthAttendances = repository.findByEmpIdAndCheckInBetween(
                    ea.getEmpId(), startMonth, endMonth);
            long presentDays = monthAttendances.stream().filter(a -> "YES".equalsIgnoreCase(a.getPresent())).count();
            long totalWorkingMins = monthAttendances.stream()
                    .filter(a -> a.getWorkingMins() != null && "YES".equalsIgnoreCase(a.getPresent()))
                    .mapToLong(EmployeeAttendance::getWorkingMins)
                    .sum();

            List<EmployeeDuty> monthDuties = dutyRepository.findByEmpIdAndDutyDateBetween(
                    ea.getEmpId(), firstOfMonth, lastOfMonth);
            long totalScheduledDutyDays = monthDuties.size();

            attendanceRate = (totalScheduledDutyDays > 0)
                    ? ((double) presentDays / totalScheduledDutyDays) * 100.0
                    : 0.0;
            avgHours = (presentDays > 0)
                    ? (totalWorkingMins / 60.0) / presentDays
                    : 0.0;
            ea.setAttendanceRate(attendanceRate);
            ea.setAvgHours(avgHours);
        }

        EmployeeAttendance saved = repository.save(ea);
        return mapToResponseDTO(saved);
    }
    @Override
    public EmployeeAttendanceResponseDTO update(String id, EmployeeAttendanceUpdateDTO dto) {
        EmployeeAttendance ea = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        if (dto.getCheckIn() != null) ea.setCheckIn(toIST(dto.getCheckIn()));
        if (dto.getCheckOut() != null) ea.setCheckOut(toIST(dto.getCheckOut()));
        if (dto.getBreakIn() != null) ea.setBreakIn(toIST(dto.getBreakIn()));
        if (dto.getBreakOut() != null) ea.setBreakOut(toIST(dto.getBreakOut()));
        if (dto.getDescription() != null) ea.setDescription(dto.getDescription());

        ea.setUpdatedAt(LocalDateTime.now(IST_ZONE));

        EmployeeAttendance saved = repository.save(ea);
        return mapToResponseDTO(saved);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public EmployeeAttendanceResponseDTO getById(String id) {
        return repository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));
    }

    @Override
    public List<EmployeeAttendanceResponseDTO> getAll() {
        return repository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    public List<EmployeeAttendanceResponseDTO> getByEmpId(String empId) {
        return repository.findByEmpId(empId).stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    public List<EmployeeAttendanceResponseDTO> getByEmpIdOneDay(String empId, LocalDateTime day) {
        LocalDateTime s = toIST(day.withHour(0).withMinute(0).withSecond(0).withNano(0));
        LocalDateTime e = s.plusDays(1);
        return repository.findByEmpIdAndCheckInBetween(empId, s, e)
                .stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    public List<EmployeeAttendanceResponseDTO> getByEmpIdOneWeek(String empId, LocalDateTime reference) {
        LocalDateTime s = toIST(reference.withHour(0).withMinute(0).withSecond(0).withNano(0)
                .minusDays(reference.getDayOfWeek().getValue() - 1));
        LocalDateTime e = s.plusWeeks(1);
        return repository.findByEmpIdAndCheckInBetween(empId, s, e)
                .stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    public List<EmployeeAttendanceResponseDTO> getByEmpIdOneMonth(String empId, LocalDateTime reference) {
        LocalDateTime s = toIST(reference.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        LocalDateTime e = s.plusMonths(1);
        return repository.findByEmpIdAndCheckInBetween(empId, s, e)
                .stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    public List<EmployeeAttendanceResponseDTO> getByEmpIdThreeMonths(String empId, LocalDateTime reference) {
        LocalDateTime s = toIST(reference.minusMonths(2).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        LocalDateTime e = s.plusMonths(3);
        return repository.findByEmpIdAndCheckInBetween(empId, s, e)
                .stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    public List<EmployeeAttendanceResponseDTO> getByEmpIdSixMonths(String empId, LocalDateTime reference) {
        LocalDateTime s = toIST(reference.minusMonths(5).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        LocalDateTime e = s.plusMonths(6);
        return repository.findByEmpIdAndCheckInBetween(empId, s, e)
                .stream().map(this::mapToResponseDTO).toList();
    }

    private EmployeeAttendanceResponseDTO mapToResponseDTO(EmployeeAttendance ea) {
        EmployeeAttendanceResponseDTO dto = new EmployeeAttendanceResponseDTO();
        dto.setId(ea.getId());
        dto.setEmpId(ea.getEmpId());
        dto.setUsername(ea.getUsername());
        dto.setOrganizationId(ea.getOrganizationId());
        dto.setPresent(ea.getPresent());
        dto.setAbsent(ea.getAbsent());
        dto.setAttendanceRate(ea.getAttendanceRate() != null ? String.format("%.2f%%", ea.getAttendanceRate()) : null);
        dto.setAvgHours(ea.getAvgHours() != null ? String.format("%.2f", ea.getAvgHours()) : null);
        dto.setCheckIn(formatLocalDateTime(ea.getCheckIn()));   // <-- FIX
        dto.setCheckOut(formatLocalDateTime(ea.getCheckOut())); // <-- FIX
        dto.setActuallyWorkingHours(minutesToHourMinFormat(ea.getActuallyWorkingHoursMins()));
        dto.setWorking(minutesToHourMinFormat(ea.getWorkingMins()));
        dto.setShortTime(minutesToHourMinFormat(ea.getShortTimeMins()));
        dto.setExtraHours(minutesToHourMinFormat(ea.getExtraHoursMins()));
        dto.setBreakTime(minutesToHourMinFormat(ea.getBreakTimeMins()));
        dto.setDescription(ea.getDescription());
        return dto;
    }

    private String formatLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.toString(); // or .format(...) for your preferred string format
    }

}
