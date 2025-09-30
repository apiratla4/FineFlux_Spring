// src/main/java/com/pulse/fineflux/repository/DocumentRepository.java
package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.DocumentRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DocumentRepository extends MongoRepository<DocumentRecord, String> {
}
