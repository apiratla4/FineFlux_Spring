package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.StockRegister;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StockRegisterRepository extends MongoRepository<StockRegister, String> {

    List<StockRegister> findByOrganizationId(String organizationId);

}
