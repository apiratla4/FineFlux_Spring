package com.pulse.fineflux.repository;


import com.pulse.fineflux.entity.ProfitLoss;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfitLossRepository extends MongoRepository<ProfitLoss, String> {
    Optional<ProfitLoss> findTopByOrderByCalculatedAtDesc();
}
