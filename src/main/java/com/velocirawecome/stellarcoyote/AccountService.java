package com.velocirawecome.stellarcoyote;

import com.velocirawecome.stellarcoyote.TransactionRepository.UserTransactionCount;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Service
public class AccountService {
    

    private final TransactionRepository transactionRepository;

    public AccountService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Flux<UserTransactionCount> listAccounts() {
        return transactionRepository.findUserTransactionCounts();
    }

    public Mono<Long> getBalance(String account, LocalDateTime upTo) {
        return transactionRepository.getBalance(account, upTo);
    }
    
    // time doesn't move forward so use last record time as 'now'
    @Cacheable("defaultCache")
    public Mono<LocalDateTime> getNow() {
        return transactionRepository.getMaxTimestamp();
    }
}
