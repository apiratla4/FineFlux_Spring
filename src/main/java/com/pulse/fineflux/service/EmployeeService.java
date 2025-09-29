// src/main/java/com/pulse/fineflux/service/EmployeeService.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.EmployeeCreateRequest;
import com.pulse.fineflux.domain.EmployeeResponse;
import com.pulse.fineflux.domain.EmployeeUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeResponse create(EmployeeCreateRequest req);
    EmployeeResponse get(String id);
    Page<EmployeeResponse> list(Pageable pageable);
    EmployeeResponse update(String id, EmployeeUpdateRequest req);
    void delete(String id);
}
