package com.pulse.fineflux.config;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class GcsStorageConfig {

    @Value("${gcs.bucket.name}")
    private String bucketName;

    @Value("${gcs.bucket.serviceAccount.filename}")
    private String serviceAccountFileName;

    @Value("${gcs.service.account.json:}")
    private String serviceAccountJson;

    @Bean
    @Primary
    public Storage defaultStorage() {
        return StorageOptions.getDefaultInstance().getService();
    }

    @Bean("serviceAccountStorage")
    public Storage serviceAccountStorage(Storage defaultStorage) throws IOException {
        // Try to use environment variable first
        if (serviceAccountJson != null && !serviceAccountJson.isEmpty()) {
            try (InputStream credentialsStream = new ByteArrayInputStream(
                    serviceAccountJson.getBytes(StandardCharsets.UTF_8))) {
                return StorageOptions.newBuilder()
                        .setCredentials(ServiceAccountCredentials.fromStream(credentialsStream))
                        .build()
                        .getService();
            }
        }

        // Fallback: Try to fetch from GCS bucket
        try {
            Blob blob = defaultStorage.get(BlobId.of(bucketName, serviceAccountFileName));
            if (blob != null && blob.exists()) {
                byte[] content = blob.getContent();
                try (InputStream credentialsStream = new ByteArrayInputStream(content)) {
                    return StorageOptions.newBuilder()
                            .setCredentials(ServiceAccountCredentials.fromStream(credentialsStream))
                            .build()
                            .getService();
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to fetch service account from GCS: " + e.getMessage());
        }

        // Final fallback: Return default storage (uses application default credentials)
        System.err.println("Warning: Using default storage credentials. Set GCS_SERVICE_ACCOUNT_JSON environment variable for proper authentication.");
        return defaultStorage();
    }
}
