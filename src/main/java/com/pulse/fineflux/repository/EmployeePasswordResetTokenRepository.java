package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.EmployeePasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface EmployeePasswordResetTokenRepository extends MongoRepository<EmployeePasswordResetToken, String> {
    Optional<EmployeePasswordResetToken> findByTokenAndOrgId(String token, String orgId);
}

