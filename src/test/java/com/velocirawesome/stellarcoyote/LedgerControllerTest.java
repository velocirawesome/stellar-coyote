package com.velocirawesome.stellarcoyote;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Controller only test, checks parameter and response marshalling.
 */
@WebFluxTest(LedgerController.class)
@Import(PermaNowClockConfig.class)
public class LedgerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private LedgerService mockLedgerService;
    
    @Autowired
    private Clock clock;
    
    String account = "test-account";
    String pointInTimeString = "2024-03-10T12:30:00";
    LocalDateTime pointInTime = LocalDateTime.parse(pointInTimeString);
    int lookbackDays = 180;
    Duration lookback = Duration.ofDays(lookbackDays);

    PredictionResult stubPrediction = new PredictionResult(345.67, 340.0, pointInTime, lookback, account, 1.0);

    private LocalDateTime NOW;

    @BeforeEach
    void setUp() {
        NOW = LocalDateTime.now(clock);
        
    }

    @Test
    void listAccounts_shouldReturnFluxOfUserTransactionCount() {
        // Arrange
        MockUserTransactionCount count1 = new MockUserTransactionCount("User1", 10L, "acc1");
        MockUserTransactionCount count2 = new MockUserTransactionCount("User2", 20L, "acc2");
        
        when(mockLedgerService.listAccounts()).thenReturn(Flux.just(count1, count2));

        // Act & Assert
        webTestClient.get().uri("/account")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MockUserTransactionCount.class)
                .hasSize(2)
                .contains(count1, count2);
    }

    @Test
    void getBalance_withAllParams_shouldReturnPredictionResult() {

        when(mockLedgerService.getBalance(eq(account), eq(NOW.plusDays(10)), eq(lookback)))
                .thenReturn(Mono.just(stubPrediction));

        // Act & Assert
        webTestClient.get().uri(uriBuilder -> uriBuilder
                        .path("/account/{account}/balance")
                        .queryParam("daysOffset", 10)
                        .queryParam("lookbackDays", String.valueOf(lookbackDays))
                        .build(account))
                .exchange()
                .expectStatus().isOk()
                .expectBody(PredictionResult.class)
                .isEqualTo(stubPrediction);
    }


    @Test
    // lookback defaults to 537 days, pointInTime defaults to 21st June 2020
    void getBalance_withNoOptionalParams_shouldUseDefaultsAndReturnPredictionResult() {

        when(mockLedgerService.getBalance(eq(account), eq(NOW), eq(Duration.ofDays(537))))
        .thenReturn(Mono.just(stubPrediction));
        
        webTestClient.get().uri("/account/{account}/balance", account)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PredictionResult.class)
                .isEqualTo(stubPrediction);
    }
}
