package com.pulse.fineflux.controller;

import com.pulse.fineflux.domain.DocumentCreateRequest;
import com.pulse.fineflux.domain.DocumentUpdateRequest;
import com.pulse.fineflux.domain.DocumentResponse;
import com.pulse.fineflux.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/documents")
// Allow the dev frontend to call these endpoints. Change or tighten origin in production.
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class DocumentController {

    private final DocumentService service;
    private final RestTemplate restTemplate = new RestTemplate();

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

    // 2. Generate signed download URL for file (returns the signed URL string)
    @GetMapping("/{documentId}/download-url")
    public ResponseEntity<String> getDownloadUrl(
            @PathVariable("orgId") String orgId,
            @PathVariable("documentId") String documentId,
            @RequestParam(value = "durationSeconds", defaultValue = "60") int durationSeconds) {
        // Keep existing service call (service.generateDownloadUrl(documentId, durationSeconds))
        // If your service signature requires orgId you can forward it there instead.
        String url = service.generateDownloadUrl(documentId, durationSeconds);
        return ResponseEntity.ok(url);
    }

    // New: 2b. Redirect to signed URL (browser navigates to the signed URL)
    // Useful for letting the browser handle content-disposition / inline display.
    @GetMapping("/{documentId}/download")
    public ResponseEntity<Void> redirectToSignedUrl(
            @PathVariable("orgId") String orgId,
            @PathVariable("documentId") String documentId,
            @RequestParam(value = "durationSeconds", defaultValue = "60") int durationSeconds) {

        String signedUrl = service.generateDownloadUrl(documentId, durationSeconds);
        if (signedUrl == null || signedUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(signedUrl)).build();
    }

    // New: 2c. Stream / proxy the remote object via the server.
    // This avoids CORS and makes inline preview reliable for the frontend.
    @GetMapping("/{documentId}/stream")
    public ResponseEntity<byte[]> streamDocumentProxy(
            @PathVariable("orgId") String orgId,
            @PathVariable("documentId") String documentId,
            @RequestParam(value = "durationSeconds", defaultValue = "300") int durationSeconds) {

        String signedUrl = service.generateDownloadUrl(documentId, durationSeconds);
        if (signedUrl == null || signedUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Fetch bytes from signed URL server-side
            ResponseEntity<byte[]> resp = restTemplate.getForEntity(signedUrl, byte[].class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }

            HttpHeaders headers = new HttpHeaders();
            // Propagate content type if present
            MediaType contentType = resp.getHeaders().getContentType();
            if (contentType != null) headers.setContentType(contentType);
            // Propagate filename/content-disposition if available
            List<String> cd = resp.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION);
            if (cd != null && !cd.isEmpty()) {
                headers.put(HttpHeaders.CONTENT_DISPOSITION, cd);
            } else {
                // try to set a safe inline disposition with the documentId as filename
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + documentId + "\"");
            }

            return new ResponseEntity<>(resp.getBody(), headers, HttpStatus.OK);

        } catch (Exception ex) {
            log.error("Failed to proxy stream for docId={} orgId={}: {}", documentId, orgId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(("Failed to fetch document: " + ex.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
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