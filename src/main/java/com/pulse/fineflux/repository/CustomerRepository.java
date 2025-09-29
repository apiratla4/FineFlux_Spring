
package com.pulse.fineflux.repository;

import com.pulse.fineflux.entity.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<Customer, String> {
}
