package com.pulse.fineflux.service;

import com.pulse.fineflux.entity.EmployeeNewTask;
import java.util.List;

public interface EmployeeNewTaskService {
    EmployeeNewTask createTask(EmployeeNewTask task);
    List<EmployeeNewTask> getTasksForEmployee(String orgId, String empId, String status);
    List<EmployeeNewTask> getAllTasks(String orgId);
    EmployeeNewTask updateTaskStatus(String taskId, String status);
}
