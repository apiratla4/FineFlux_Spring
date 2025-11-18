package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.DocumentCreateRequest;
import com.pulse.fineflux.domain.DocumentUpdateRequest;
import com.pulse.fineflux.domain.DocumentResponse;
import com.pulse.fineflux.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    // 1. Upload document file to GCS
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocumentFile(
            @PathVariable("orgId") String orgId,
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        log.info("API: Upload file orgId={} name={}", orgId, file.getOriginalFilename());
        String fileUrl = service.uploadFileToGcs(orgId, file);
        return ResponseEntity.ok(fileUrl);
    }

    // 2. Generate signed download URL for file
    @GetMapping("/{documentId}/download-url")
    public ResponseEntity<String> getDownloadUrl(
            @PathVariable("orgId") String orgId,
            @PathVariable("documentId") String documentId,
            @RequestParam(value = "durationSeconds", defaultValue = "60") int durationSeconds) {
        String url = service.generateDownloadUrl(documentId, durationSeconds);
        return ResponseEntity.ok(url);
    }

    // 3. List documents
    @GetMapping
    public Page<DocumentResponse> list(@PathVariable("orgId") String orgId, Pageable pageable) {
        log.debug("API: List documents orgId={}", orgId);
        return service.list(orgId, pageable);
    }

    // 4. Get document metadata
    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable("orgId") String orgId, @PathVariable String id) {
        log.debug("API: Get doc orgId={} id={}", orgId, id);
        return service.get(orgId, id);
    }

    // 5. Create document metadata (use this after upload for metadata save)
    @PostMapping
    public DocumentResponse create(@PathVariable("orgId") String orgId, @RequestBody DocumentCreateRequest req) {
        log.info("API: Create doc orgId={}", orgId);
        return service.create(orgId, req);
    }

    // 6. Update document metadata
    @PutMapping("/{id}")
    public DocumentResponse update(@PathVariable("orgId") String orgId, @PathVariable String id, @RequestBody DocumentUpdateRequest req) {
        log.info("API: Update doc orgId={} id={}", orgId, id);
        return service.update(orgId, id, req);
    }

    // 7. Delete document
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("orgId") String orgId, @PathVariable String id) {
        log.info("API: Delete doc orgId={} id={}", orgId, id);
        service.delete(orgId, id);
    }
}
