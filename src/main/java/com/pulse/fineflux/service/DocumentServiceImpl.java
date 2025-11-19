package com.pulse.fineflux.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.SignUrlOption;
import com.pulse.fineflux.domain.DocumentCreateRequest;
import com.pulse.fineflux.domain.DocumentResponse;
import com.pulse.fineflux.domain.DocumentUpdateRequest;
import com.pulse.fineflux.entity.DocumentRecord;
import com.pulse.fineflux.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository repo;

    @Autowired
    @Qualifier("serviceAccountStorage")
    private Storage storage;

    @Value("${gcs.bucket.name}")
    private String bucketName;

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

    /**
     * Uploads the file into GCS under a folder named after the organizationId.
     * Also sets Content-Disposition metadata so downloads suggest the original filename.
     *
     * @param organizationId organization id (used as prefix/folder in object name)
     * @param file           multipart file
     * @return a public-ish file URL (https://storage.googleapis.com/{bucket}/{blobName})
     * @throws Exception on any storage/upload error
     */
    public String uploadFileToGcs(String organizationId, MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            originalFilename = "file";
        }
        // sanitize a little for safety (replace newlines / excessive spaces)
        String safeName = originalFilename.replaceAll("[\\r\\n]+", "_").trim().replaceAll("\\s+", "_");

        log.info("Uploading to GCS: orgId={}, fileName={}", organizationId, safeName);
        String folder = organizationId;
        String blobName = folder + "/" + System.currentTimeMillis() + "_" + safeName;

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, blobName)
                .setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType())
                .setContentDisposition("attachment; filename=\"" + safeName + "\"")
                .setCacheControl("private, max-age=0, no-transform")
                .build();

        storage.create(blobInfo, file.getBytes());

        String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + blobName;
        log.info("Upload finished, url={}", fileUrl);
        return fileUrl;
    }

    /**
     * Generates a signed temporary download URL for a file stored on GCS.
     * Example usage: get via documentId, extract blob path from fileUrl.
     *
     * @param documentId      The database id of the document
     * @param durationSeconds Validity period in seconds for the download url
     * @return signed URL for download
     */
    public String generateDownloadUrl(String documentId, int durationSeconds) {
        DocumentRecord doc = repo.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        String fileUrl = doc.getFileUrl();
        String gcsObjectPath = extractBlobNameFromUrl(fileUrl);

        // Build a minimal BlobInfo for signing. Metadata like contentDisposition is stored with the blob
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, gcsObjectPath).build();
        URL signedUrl = storage.signUrl(blobInfo, durationSeconds, TimeUnit.SECONDS, SignUrlOption.withV4Signature());
        log.info("Generated signed download URL for documentId={} url={}", documentId, signedUrl);
        return signedUrl.toString();
    }

    private String extractBlobNameFromUrl(String fileUrl) {
        String prefix = "https://storage.googleapis.com/" + bucketName + "/";
        if (fileUrl != null && fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid GCS URL format: " + fileUrl);
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
        r.organizationId = e.getOrganizationId();
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