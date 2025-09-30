// src/main/java/com/pulse/fineflux/controller/EmployeeController.java
package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.EmployeeCreateRequest;
import com.pulse.fineflux.domain.EmployeeResponse;
import com.pulse.fineflux.domain.EmployeeUpdateRequest;
import com.pulse.fineflux.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody EmployeeCreateRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @GetMapping
    public Page<EmployeeResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable String id, @Valid @RequestBody EmployeeUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
