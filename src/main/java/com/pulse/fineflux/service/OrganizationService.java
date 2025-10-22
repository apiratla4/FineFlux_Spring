// src/main/java/com/pulse/fineflux/service/OrganizationService.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.OrganizationCreateRequest;
import com.pulse.fineflux.domain.OrganizationResponse;
import com.pulse.fineflux.domain.OrganizationUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationService {
    OrganizationResponse create(OrganizationCreateRequest req);
    OrganizationResponse getById(String id);
    OrganizationResponse getByOrgId(String organizationId);
    Page<OrganizationResponse> list(Pageable pageable);
    OrganizationResponse update(String id, OrganizationUpdateRequest req);
    void delete(String id);
}
