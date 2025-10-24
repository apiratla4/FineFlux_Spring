package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.DensityRegister;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DensityRegisterRepository extends MongoRepository<DensityRegister, String> {
    List<DensityRegister> findByOrganizationId(String organizationId);
}
