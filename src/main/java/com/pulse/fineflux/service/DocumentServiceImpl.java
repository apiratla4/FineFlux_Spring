// src/main/java/com/pulse/fineflux/service/impl/DocumentServiceImpl.java
package com.pulse.fineflux.service;

import com.pulse.fineflux.domain.DocumentCreateRequest;
import com.pulse.fineflux.domain.DocumentResponse;
import com.pulse.fineflux.domain.DocumentUpdateRequest;
import com.pulse.fineflux.entity.DocumentRecord;
import com.pulse.fineflux.repository.DocumentRepository;
import com.pulse.fineflux.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository repo;

    public DocumentServiceImpl(DocumentRepository repo) {
        this.repo = repo;
    }

    @Override
    public DocumentResponse create(String organizationId, DocumentCreateRequest req) {
        log.info("Creating document orgId={} type={}", organizationId, req.documentType);
        DocumentRecord d = new DocumentRecord();
        d.setOrganizationId(organizationId);
        d.setDocumentType(req.documentType);
        d.setIssuingAuthority(req.issuingAuthority);
        d.setIssuedDate(req.issuedDate);
        d.setExpiryDate(req.expiryDate);
        d.setRenewalPeriodDays(req.renewalPeriodDays);
        d.setResponsibleParty(req.responsibleParty);
        d.setFileUrl(req.fileUrl);
        d.setNotes(req.notes);
        d = repo.save(d);
        log.info("Created document id={} orgId={}", d.getId(), organizationId);
        return toResponse(d);
    }

    @Override
    public DocumentResponse get(String organizationId, String id) {
        log.debug("Fetching document id={} orgId={}", id, organizationId);
        DocumentRecord d = repo.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> {
                    log.warn("Document not found id={} orgId={}", id, organizationId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
                });
        return toResponse(d);
    }

    @Override
    public Page<DocumentResponse> list(String organizationId, Pageable pageable) {
        log.debug("Listing documents orgId={} page={} size={}", organizationId, pageable.getPageNumber(), pageable.getPageSize());
        return repo.findAllByOrganizationId(organizationId, pageable).map(this::toResponse);
    }

    @Override
    public DocumentResponse update(String organizationId, String id, DocumentUpdateRequest req) {
        log.info("Updating document id={} orgId={}", id, organizationId);
        DocumentRecord d = repo.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> {
                    log.warn("Document not found id={} orgId={}", id, organizationId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
                });

        if (req.documentType != null) d.setDocumentType(req.documentType);
        if (req.issuingAuthority != null) d.setIssuingAuthority(req.issuingAuthority);
        if (req.issuedDate != null) d.setIssuedDate(req.issuedDate);
        if (req.expiryDate != null) d.setExpiryDate(req.expiryDate);
        if (req.renewalPeriodDays != null) d.setRenewalPeriodDays(req.renewalPeriodDays);
        if (req.responsibleParty != null) d.setResponsibleParty(req.responsibleParty);
        if (req.fileUrl != null) d.setFileUrl(req.fileUrl);
        if (req.notes != null) d.setNotes(req.notes);

        d = repo.save(d);
        log.info("Updated document id={} orgId={}", id, organizationId);
        return toResponse(d);
    }

    @Override
    public void delete(String organizationId, String id) {
        log.info("Deleting document id={} orgId={}", id, organizationId);
        if (!repo.existsByIdAndOrganizationId(id, organizationId)) {
            log.warn("Delete failed, document not found id={} orgId={}", id, organizationId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
        }
        repo.deleteById(id);
        log.info("Deleted document id={} orgId={}", id, organizationId);
    }

    private DocumentResponse toResponse(DocumentRecord e) {
        DocumentResponse r = new DocumentResponse();
        r.id = e.getId();
        r.documentType = e.getDocumentType();
        r.issuingAuthority = e.getIssuingAuthority();
        r.issuedDate = e.getIssuedDate();
        r.expiryDate = e.getExpiryDate();
        r.renewalPeriodDays = e.getRenewalPeriodDays();
        r.responsibleParty = e.getResponsibleParty();
        r.fileUrl = e.getFileUrl();
        r.notes = e.getNotes();
        return r;
    }
}
