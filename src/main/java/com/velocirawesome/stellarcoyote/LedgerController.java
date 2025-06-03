package com.velocirawesome.stellarcoyote;

import com.velocirawesome.stellarcoyote.LedgerRepository.UserTransactionCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@RestController
public class LedgerController {
    
    private final LedgerService ledgerService;
    
    public static final LocalDateTime PERMA_NOW = LocalDateTime.of(2024, 3, 10, 12, 30, 0);

    public LedgerController(LedgerService accountService) {
        this.ledgerService = accountService;
    }
    
    @GetMapping("/account")
    public Flux<UserTransactionCount> listAccounts() {
        return ledgerService.listAccounts();
    }

    @GetMapping("/account/{account}/balance")
    public Mono<PredictionResult> getBalance(@PathVariable String account, 
                                 @RequestParam(required = false) String pointInTime,
                                 @RequestParam(required = false, defaultValue = "537") int lookbackDays) {
        log.info("getBalance called for account: {}, pointInTime: {}, lookbackDays: {}", account, pointInTime, lookbackDays);
        
        LocalDateTime date = pointInTime != null ? LocalDateTime.parse(pointInTime) : PERMA_NOW;
        Duration lookback = Duration.ofDays(lookbackDays);
        return ledgerService.getBalance(account, date, lookback);
    }
}
