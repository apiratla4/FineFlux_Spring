package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.DocumentCreateRequest;
import com.pulse.fineflux.domain.DocumentResponse;
import com.pulse.fineflux.domain.DocumentUpdateRequest;
import com.pulse.fineflux.service.DocumentService;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import jakarta.validation.Valid;
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
    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private final String bucketName = "pulse-dev";

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    // ------------ 1. GCS File Upload -------------
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocumentFile(
            @PathVariable("orgId") String orgId,
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        log.info("Uploading file to Google Bucket for orgId={}, original name={}", orgId, file.getOriginalFilename());
        String folder = orgId;
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String objectName = folder + "/" + fileName;

        BlobInfo blobInfo = storage.create(
                BlobInfo.newBuilder(bucketName, objectName)
                        .setContentType(file.getContentType())
                        .build(),
                file.getBytes()
        );
        String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + objectName;
        log.info("Upload finished, objectName={}, url={}", objectName, fileUrl);
        return ResponseEntity.ok(fileUrl);
    }

    // ------------ 2. List Documents for Org (paged) -------------
    @GetMapping
    public Page<DocumentResponse> list(@PathVariable("orgId") String orgId, Pageable pageable) {
        log.debug("HTTP GET documents orgId={} page={} size={}", orgId, pageable.getPageNumber(), pageable.getPageSize());
        return service.list(orgId, pageable);
    }

    // ------------ 3. Get Document by Id -------------
    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable("orgId") String orgId, @PathVariable String id) {
        log.debug("HTTP GET documents/{id} id={} orgId={}", id, orgId);
        return service.get(orgId, id);
    }

    // ------------ 4. Create Document (metadata/record, not file) -------------
    @PostMapping
    public DocumentResponse create(@PathVariable("orgId") String orgId, @Valid @RequestBody DocumentCreateRequest req) {
        log.info("HTTP POST documents orgId={}", orgId);
        return service.create(orgId, req);
    }

    // ------------ 5. Update Document metadata -------------
    @PutMapping("/{id}")
    public DocumentResponse update(@PathVariable("orgId") String orgId, @PathVariable String id, @Valid @RequestBody DocumentUpdateRequest req) {
        log.info("HTTP PUT documents/{id} id={} orgId={}", id, orgId);
        return service.update(orgId, id, req);
    }

    // ------------ 6. Delete Document -------------
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("orgId") String orgId, @PathVariable String id) {
        log.info("HTTP DELETE documents/{id} id={} orgId={}", id, orgId);
        service.delete(orgId, id);
    }
}
