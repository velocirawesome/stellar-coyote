package com.velocirawecome.stellarcoyote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest

class AccountServiceCacheTest {

    @Autowired
    private AccountService accountService;

    // new annotation, who dis. Spotted this one in the release notes.
    @MockitoBean
    private TransactionRepository mockTransactionRepository;
    
    // skip csv import
    @MockitoBean
    private CsvToPostgresProcessor mockCsvToPostgresProcessor;

    @Test
    void getNowShouldUseCache() {
        LocalDateTime now = LocalDateTime.of(2020, 1, 1, 12, 0);
        when(mockTransactionRepository.getMaxTimestamp()).thenReturn(Mono.just(now));

        // First call - should hit the repository
        LocalDateTime result1 = accountService.getNow().block();
        // Second call - should use cache
        LocalDateTime result2 = accountService.getNow().block();

        assertThat(result1).isEqualTo(now);
        assertThat(result2).isEqualTo(now);
        // Should only call the repository once
        verify(mockTransactionRepository, times(1)).getMaxTimestamp();
    }
}
