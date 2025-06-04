package com.velocirawesome.stellarcoyote;

import com.velocirawesome.stellarcoyote.LedgerRepository.UserTransactionCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;


@Slf4j
@Service
public class LedgerService {
    @Autowired
    private LedgerRepository transactionRepository;
    @Autowired
    private TribuoService tribuoService;
    @Autowired
    Clock clock;

    public Flux<UserTransactionCount> listAccounts() {
        return transactionRepository.findUserTransactionCounts();
    }
    

    public Mono<PredictionResult> getBalance(String account, LocalDateTime pointInTime, Duration lookback) {
        LocalDateTime now = LocalDateTime.now(clock);
        return getFutureBalance(account, pointInTime, lookback, now)
                .flatMap(prediction -> {
                    if (pointInTime.isAfter(now)) {
                        return Mono.just(prediction);
                    } else {
                        return getHistoricalBalance(account, pointInTime)
                                .doOnNext(prediction::setActual)
                                .thenReturn(prediction);
                    }
                });
    }


    protected Mono<Double> getHistoricalBalance(String account, LocalDateTime pointInTime) {
        return transactionRepository.getBalance(account, pointInTime);
    }
    
    protected Mono<PredictionResult> getFutureBalance(String account, LocalDateTime pointInTime, Duration lookback, LocalDateTime now) {
        return tribuoService.predict(account, lookback, pointInTime, now);
    }


}
