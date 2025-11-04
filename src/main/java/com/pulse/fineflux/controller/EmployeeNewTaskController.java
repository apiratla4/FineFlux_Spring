package com.pulse.fineflux.controller;

import com.pulse.fineflux.entity.EmployeeNewTask;
import com.pulse.fineflux.service.EmployeeNewTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/tasks")
@RequiredArgsConstructor
public class EmployeeNewTaskController {
    private final EmployeeNewTaskService taskService;

    // Assign new task
    @PostMapping
    public ResponseEntity<EmployeeNewTask> assignTask(@PathVariable String orgId, @RequestBody EmployeeNewTask task) {
        task.setOrganizationId(orgId);
        return ResponseEntity.ok(taskService.createTask(task));
    }

    // List all tasks for employee by status
    @GetMapping("/employee/{empId}")
    public List<EmployeeNewTask> getEmployeeTasks(
            @PathVariable String orgId,
            @PathVariable String empId,
            @RequestParam(required = false, defaultValue = "pending") String status
    ) {
        return taskService.getTasksForEmployee(orgId, empId, status);
    }

    // List all tasks for org
    @GetMapping
    public List<EmployeeNewTask> getAllTasks(@PathVariable String orgId) {
        return taskService.getAllTasks(orgId);
    }

    // Update task status (action button in UI)
    @PutMapping("/{taskId}/status")
    public EmployeeNewTask updateTaskStatus(
            @PathVariable String orgId,
            @PathVariable String taskId,
            @RequestParam String status) {
        return taskService.updateTaskStatus(taskId, status);
    }
}
