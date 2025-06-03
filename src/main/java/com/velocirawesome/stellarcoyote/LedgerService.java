package com.velocirawesome.stellarcoyote;

import com.velocirawesome.stellarcoyote.LedgerRepository.UserTransactionCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class LedgerService {
    @Autowired
    private LedgerRepository transactionRepository;
    @Autowired
    private TribuoService tribuoService;

    public Flux<UserTransactionCount> listAccounts() {
        return transactionRepository.findUserTransactionCounts();
    }
    

    public Mono<PredictionResult> getBalance(String account, LocalDateTime pointInTime, Duration lookback) {
        return getNow()
                .flatMap(now -> 
                getFutureBalance(account, pointInTime, lookback, now)
                .flatMap(prediction -> {
                    if (pointInTime.isAfter(now)) {
                        return Mono.just(prediction);
                    } else {
                        return getHistoricalBalance(account, pointInTime)
                                .doOnNext(prediction::setActual)
                                .thenReturn(prediction);
                    }
                })
          );
    }


    protected Mono<Double> getHistoricalBalance(String account, LocalDateTime pointInTime) {
        return transactionRepository.getBalance(account, pointInTime);
    }
    
    protected Mono<PredictionResult> getFutureBalance(String account, LocalDateTime pointInTime, Duration lookback, LocalDateTime now) {
        return tribuoService.predict(account, lookback, pointInTime, now);
    }
    
    // time doesn't move forward so use last record time as 'now'
    @Cacheable("defaultCache")
    public Mono<LocalDateTime> getNow() {
        return transactionRepository.getMaxTimestamp()
                .doOnNext(now -> log.info("'Now' is {}", now))
                .cache();
    }

}
