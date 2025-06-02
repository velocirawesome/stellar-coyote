package com.velocirawecome.stellarcoyote;

import com.velocirawecome.stellarcoyote.TransactionRepository.UserTransactionCount;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ControllerController {
    
    private final AccountService accountService;

    public ControllerController(AccountService accountService) {
        this.accountService = accountService;
    }
    
    @GetMapping("/account")
    public Flux<UserTransactionCount> listAccounts() {
        return accountService.listAccounts();
    }

    @GetMapping("/account/{account}/balance")
    public Mono<Long> getBalance(@PathVariable String account, @RequestParam(name = "pointInTime", required = false) String upTo) {
        LocalDateTime upToDate = upTo != null ? LocalDateTime.parse(upTo) : LocalDateTime.now();
        return accountService.getBalance(account, upToDate);
    }
}
