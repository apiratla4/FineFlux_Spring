// src/main/java/com/pulse/fineflux/controller/DocumentController.java
package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.DocumentCreateRequest;
import com.pulse.fineflux.domain.DocumentResponse;
import com.pulse.fineflux.domain.DocumentUpdateRequest;
import com.pulse.fineflux.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    // GET all (paged)
    @GetMapping
    public Page<DocumentResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    // GET by id
    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable String id) {
        return service.get(id);
    }

    // POST create
    @PostMapping
    public DocumentResponse create(@Valid @RequestBody DocumentCreateRequest req) {
        return service.create(req);
    }

    // PUT update by id (partial update semantics)
    @PutMapping("/{id}")
    public DocumentResponse update(@PathVariable String id, @Valid @RequestBody DocumentUpdateRequest req) {
        return service.update(id, req);
    }

    // DELETE by id
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
