package com.pulse.fineflux.service;

import com.pulse.fineflux.entity.Employee;
import com.pulse.fineflux.entity.EmployeeNewTask;
import com.pulse.fineflux.repository.EmployeeNewTaskRepository;
import com.pulse.fineflux.repository.EmployeeRepository;
import com.pulse.fineflux.service.EmployeeNewTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeNewTaskServiceImpl implements EmployeeNewTaskService {
    private final EmployeeNewTaskRepository taskRepo;
    private final EmployeeRepository employeeRepo;

    @Override
    public EmployeeNewTask createTask(EmployeeNewTask task) {
        // Only assign to ACTIVE employees
        Employee emp = employeeRepo.findByEmpIdAndOrganizationId(task.getAssignedToEmpId(), task.getOrganizationId());
        if (emp == null || !"ACTIVE".equals(emp.getStatus())) {
            throw new RuntimeException("Assigned employee not found or not active");
        }
        task.setAssignedToName(emp.getFirstName() + " " + emp.getLastName());
        task.setStatus("pending");
        if (task.getDueDate() == null) task.setDueDate(LocalDate.now().plusDays(1));
        return taskRepo.save(task);
    }

    @Override
    public List<EmployeeNewTask> getTasksForEmployee(String orgId, String empId, String status) {
        return taskRepo.findByOrganizationIdAndAssignedToEmpIdAndStatus(orgId, empId, status);
    }

    @Override
    public List<EmployeeNewTask> getAllTasks(String orgId) {
        return taskRepo.findByOrganizationId(orgId);
    }

    @Override
    public EmployeeNewTask updateTaskStatus(String taskId, String status) {
        EmployeeNewTask task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(status);
        return taskRepo.save(task);
    }
}
