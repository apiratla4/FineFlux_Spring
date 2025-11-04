package com.pulse.fineflux.repository;
import com.pulse.fineflux.entity.EmployeeNewTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface EmployeeNewTaskRepository extends MongoRepository<EmployeeNewTask, String> {
    List<EmployeeNewTask> findByOrganizationIdAndAssignedToEmpIdAndStatus(String orgId, String empId, String status);
    List<EmployeeNewTask> findByOrganizationId(String orgId);
}
