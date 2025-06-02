package com.velocirawecome.stellarcoyote;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import lombok.ToString;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface TransactionRepository extends ReactiveCrudRepository<LedgerEntry, Long> {

    Flux<LedgerEntry> findByAccountAndTimestampAfter(String account, java.time.LocalDateTime timestamp);

    @Query("SELECT name, account as account, count(*) as amount FROM transactions group by name, account")
    Flux<UserTransactionCount> findUserTransactionCounts();

    @Query("SELECT SUM(amount) FROM transactions WHERE account = :account AND timestamp <= :timestamp")
    Mono<Long> getBalance(@Param("account") String account, @Param("timestamp") LocalDateTime timestamp);

    // defect: projection doesn't seem to work unless names shadow fields from Transaction types, despite using `as` above
    interface UserTransactionCount {
        String getName();
        Long getAmount();
        String getAccount();
    }
    
    // get max timestamp to use as NOW
    @Query("SELECT MAX(timestamp) FROM transactions WHERE account = :account")
    Mono<LocalDateTime> getMaxTimestamp();
}
