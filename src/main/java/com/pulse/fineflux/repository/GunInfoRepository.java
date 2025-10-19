package com.pulse.fineflux.repository;


import com.pulse.fineflux.entity.GunInfo;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GunInfoRepository extends MongoRepository<GunInfo, String> {
    List<GunInfo> findByOrganizationId(String organizationId);

    boolean existsByOrganizationIdAndGuns(@NotBlank(message = "Organization ID is required") String organizationId, String gunName);
}
