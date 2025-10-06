package com.pulse.fineflux.repository;


import com.pulse.fineflux.entity.GunInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GunInfoRepository extends MongoRepository<GunInfo, String> {
    List<GunInfo> findByOrganizationId(String organizationId);
}
