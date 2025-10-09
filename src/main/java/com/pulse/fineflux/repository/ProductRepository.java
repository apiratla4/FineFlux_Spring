package com.pulse.fineflux.repository;
import com.pulse.fineflux.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByOrganizationId(String organizationId);
    Optional<Product> findByIdAndOrganizationId(String id, String organizationId);
}
