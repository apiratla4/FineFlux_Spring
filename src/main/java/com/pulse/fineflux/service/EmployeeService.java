// src/main/java/com/pulse/fineflux/service/EmployeeService.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.ChangePasswordRequest;
import com.pulse.fineflux.domain.EmployeeCreateRequest;
import com.pulse.fineflux.domain.EmployeeResponse;
import com.pulse.fineflux.domain.EmployeeUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeResponse create(String organizationId, EmployeeCreateRequest req);
    EmployeeResponse get(String organizationId, String id);
    Page<EmployeeResponse> list(String organizationId, Pageable pageable);
    EmployeeResponse update(String organizationId, String id, EmployeeUpdateRequest req);
    void delete(String organizationId, String id);
    void changePassword(String orgId, String employeeId, ChangePasswordRequest req);

}
