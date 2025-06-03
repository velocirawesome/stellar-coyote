package com.velocirawesome.stellarcoyote;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface LedgerRepository extends ReactiveCrudRepository<LedgerEntry, Long> {

    Flux<LedgerEntry> findByAccountAndTimestampAfter(String account, java.time.LocalDateTime timestamp);

    @Query("SELECT name, account as account, count(*) as amount FROM ledger group by name, account")
    Flux<UserTransactionCount> findUserTransactionCounts();

    @Query("SELECT SUM(amount) FROM ledger WHERE account = :account AND timestamp <= :timestamp")
    Mono<Double> getBalance(@Param("account") String account, @Param("timestamp") LocalDateTime timestamp);

    @Query("SELECT * FROM ledger WHERE account = :account AND timestamp BETWEEN :start AND :end ORDER BY timestamp")
    Flux<LedgerEntry> findByAccountAndTimestampBetween(String account, LocalDateTime start, LocalDateTime end);
    
    // defect: projection doesn't seem to work unless names shadow fields from Transaction types, despite using `as` above
    interface UserTransactionCount {
        String getName();
        Long getAmount();
        String getAccount();
    }
    
    // get max timestamp to use as NOW
    @Query("SELECT MAX(timestamp) FROM ledger")
    Mono<LocalDateTime> getMaxTimestamp();
}
