package com.velocirawesome.stellarcoyote;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EnableCaching
@ActiveProfiles("test")
public class TribuoModelTrainerCacheTest {

    
    @MockitoSpyBean
    private TribuoModelTrainer spyTrainer;

    @MockitoBean
    private LedgerRepository ledgerRepo;
    
    @MockitoBean
    private CsvToPostgresProcessor mockCsvToPostgresProcessor;


    @Test
    void testTrainModelIsCalledOnlyOnceForSameArguments() {
        // Arrange
        String account = "test-account";
        LocalDateTime minDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime maxDate = LocalDateTime.of(2020, 6, 1, 0, 0);
        
        LedgerEntry stubEntry = new LedgerEntry(1L, minDate, account, 1.0, "user", 1.0);
        // model setup needs >=4 entries to avoid div/0
        Flux<LedgerEntry> stubTrainingSet = Flux.just(stubEntry, stubEntry, stubEntry, stubEntry);

        // Mock repository to return a Flux with a mock LedgerEntry
        Mockito.when(ledgerRepo.findByAccountAndTimestampBetween(account, minDate, maxDate))
                .thenReturn(stubTrainingSet);

        // Act: Call twice with same arguments
        spyTrainer.fetchOrTrainModel(account, minDate, maxDate).block();
        spyTrainer.fetchOrTrainModel(account, minDate, maxDate).block();

        verify(spyTrainer, times(1)).trainModel(any());
    }
}
