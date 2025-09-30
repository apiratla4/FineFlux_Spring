// src/main/java/com/pulse/fineflux/service/impl/DocumentServiceImpl.java
package com.pulse.fineflux.service.impl;

import com.pulse.fineflux.domain.*;
import com.pulse.fineflux.entity.DocumentRecord;
import com.pulse.fineflux.repository.DocumentRepository;
import com.pulse.fineflux.service.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository repo;

    public DocumentServiceImpl(DocumentRepository repo) {
        this.repo = repo;
    }

    @Override
    public DocumentResponse create(DocumentCreateRequest req) {
        DocumentRecord d = new DocumentRecord();
        d.setDocumentType(req.documentType);
        d.setIssuingAuthority(req.issuingAuthority);
        d.setIssuedDate(req.issuedDate);
        d.setExpiryDate(req.expiryDate);
        d.setRenewalPeriodDays(req.renewalPeriodDays);
        d.setResponsibleParty(req.responsibleParty);
        d.setFileUrl(req.fileUrl);
        d.setNotes(req.notes);
        d = repo.save(d);
        return toResponse(d);
    }

    @Override
    public DocumentResponse get(String id) {
        DocumentRecord d = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        return toResponse(d);
    }

    @Override
    public Page<DocumentResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(this::toResponse);
    }

    @Override
    public DocumentResponse update(String id, DocumentUpdateRequest req) {
        DocumentRecord d = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        if (req.documentType != null) d.setDocumentType(req.documentType);
        if (req.issuingAuthority != null) d.setIssuingAuthority(req.issuingAuthority);
        if (req.issuedDate != null) d.setIssuedDate(req.issuedDate);
        if (req.expiryDate != null) d.setExpiryDate(req.expiryDate);
        if (req.renewalPeriodDays != null) d.setRenewalPeriodDays(req.renewalPeriodDays);
        if (req.responsibleParty != null) d.setResponsibleParty(req.responsibleParty);
        if (req.fileUrl != null) d.setFileUrl(req.fileUrl);
        if (req.notes != null) d.setNotes(req.notes);

        d = repo.save(d);
        return toResponse(d);
    }

    @Override
    public void delete(String id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
        }
        repo.deleteById(id);
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
