// src/main/java/com/pulse/fineflux/controller/CustomerController.java
package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.CustomerCreateRequest;
import com.pulse.fineflux.domain.CustomerResponse;
import com.pulse.fineflux.domain.CustomerUpdateRequest;
import com.pulse.fineflux.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    // GET all (paged)
    @GetMapping
    public Page<CustomerResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    // GET by id
    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable String id) {
        return service.get(id);
    }

    // POST create
    @PostMapping
    public CustomerResponse create(@Valid @RequestBody CustomerCreateRequest req) {
        return service.create(req);
    }

    // PUT update by id (partial update semantics)
    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable String id, @Valid @RequestBody CustomerUpdateRequest req) {
        return service.update(id, req);
    }

    // DELETE by id
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
