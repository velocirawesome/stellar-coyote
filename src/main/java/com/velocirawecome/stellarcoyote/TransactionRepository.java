package com.velocirawecome.stellarcoyote;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends ReactiveCrudRepository<Transaction, Long> {
    // You can add custom query methods here if needed
}
