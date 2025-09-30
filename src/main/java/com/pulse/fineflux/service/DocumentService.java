// src/main/java/com/pulse/fineflux/service/DocumentService.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.DocumentCreateRequest;
import com.pulse.fineflux.domain.DocumentResponse;
import com.pulse.fineflux.domain.DocumentUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentService {
    DocumentResponse create(String organizationId, DocumentCreateRequest req);
    DocumentResponse get(String organizationId, String id);
    Page<DocumentResponse> list(String organizationId, Pageable pageable);
    DocumentResponse update(String organizationId, String id, DocumentUpdateRequest req);
    void delete(String organizationId, String id);
}
