package com.velocirawesome.stellarcoyote;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EnableCaching
@ActiveProfiles("test")
public class TriobuoModelTrainerCacheTest {

    @MockitoSpyBean
    private TriobuoModelTrainer spyTrainer;

    @Autowired
    private LedgerRepository ledgerRepo;


    @Test
    void testTrainModelIsCalledOnlyOnceForSameArguments() {
        // Arrange
        String account = "test-account";
        LocalDateTime minDate = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime maxDate = LocalDateTime.of(2020, 6, 1, 0, 0);

        // Mock repository to return a Flux with a mock LedgerEntry
        Mockito.when(ledgerRepo.findByAccountAndTimestampBetween(account, minDate, maxDate))
                .thenReturn(Flux.just(mock(LedgerEntry.class)));

        // Act: Call twice with same arguments
        Mono<TrainingResult> first = spyTrainer.fetchOrTrainModel(account, minDate, maxDate);
        Mono<TrainingResult> second = spyTrainer.fetchOrTrainModel(account, minDate, maxDate);

        // Block to trigger execution
        first.block();
        second.block();

        // Assert: trainModel should be called only once
        verify(spyTrainer, times(1)).trainModel(anyList());
    }
}
