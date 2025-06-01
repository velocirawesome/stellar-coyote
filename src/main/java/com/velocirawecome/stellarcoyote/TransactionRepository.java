package com.velocirawecome.stellarcoyote;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.util.List;

import lombok.ToString;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface TransactionRepository extends ReactiveCrudRepository<Transaction, Long> {

    Flux<Transaction> findByAccountAndTimestampAfter(String account, java.time.LocalDateTime timestamp);

    @Query("SELECT account as account, count(*) as amount FROM transactions group by account")
    Flux<UserTransactionCount> findUserTransactionCounts();

    
    // defect: projection doesn't seem to work unless names shadow fields from Transaction types
    interface UserTransactionCount {
        Long getAmount();
        String getAccount();

    }
}
