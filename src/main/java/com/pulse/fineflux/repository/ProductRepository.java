package com.pulse.fineflux.repository;
import com.pulse.fineflux.entity.Product;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByOrganizationId(String organizationId);
    Optional<Product> findByIdAndOrganizationId(String id, String organizationId);


    boolean existsByOrganizationIdAndProductName(@NotBlank(message = "Organization ID is required") String organizationId, String productName);
}
