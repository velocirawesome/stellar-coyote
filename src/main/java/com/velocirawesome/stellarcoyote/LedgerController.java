package com.velocirawesome.stellarcoyote;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.velocirawesome.stellarcoyote.LedgerRepository.UserTransactionCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@RestController
public class LedgerController {
    
    private LedgerService ledgerService;
    
    // data set happens to be 537 days long
    public static final String MAX_LOOKBACK = "537";

    private Clock clock;

    public LedgerController(LedgerService accountService, Clock clock) {
        this.ledgerService = accountService;
        this.clock = clock;
    }
    
    @GetMapping("/account")
    public Flux<UserTransactionCount> listAccounts() {
        return ledgerService.listAccounts();
    }
    
    // seeing time is a fixed point, offsetDays is days + or - from PERMA_NOW to make it easier to play with
    

    @GetMapping("/account/{account}/balance")
    public Mono<PredictionResult> getBalance(@PathVariable String account, 
                                 @RequestParam(required = false, defaultValue = "0") int offsetDays,
                                 @RequestParam(required = false, defaultValue = MAX_LOOKBACK) int lookbackDays) {
        log.info("getBalance called for account: {}, timeOffset: {}, lookbackDays: {}", account, offsetDays, lookbackDays);
        
        LocalDateTime date = LocalDateTime.now(clock).plusDays(offsetDays);
        Duration lookback = Duration.ofDays(lookbackDays);
        return ledgerService.getBalance(account, date, lookback);
    }
}
