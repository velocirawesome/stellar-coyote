package com.velocirawesome.stellarcoyote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

import static com.velocirawesome.stellarcoyote.LedgerController.PERMA_NOW;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Controller only test, checks parameter and response marshalling.
 */
@WebFluxTest(LedgerController.class)
public class LedgerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private LedgerService ledgerService;
    
    String account = "test-account";
    String pointInTimeString = "2024-03-10T12:30:00";
    LocalDateTime pointInTime;
    int lookbackDays = 180;
    Duration lookback = Duration.ofDays(lookbackDays);

    PredictionResult mockPrediction;

    {
        // Extraction block to initialize pointInTime and mockPrediction
        pointInTime = LocalDateTime.parse(pointInTimeString);
        mockPrediction = new PredictionResult(345.67, 340.0, pointInTime, lookback, account, 1.0);
    }

    @Test
    void listAccounts_shouldReturnFluxOfUserTransactionCount() {
        // Arrange
        MockUserTransactionCount count1 = new MockUserTransactionCount("User1", 10L, "acc1");
        MockUserTransactionCount count2 = new MockUserTransactionCount("User2", 20L, "acc2");
        
        when(ledgerService.listAccounts()).thenReturn(Flux.just(count1, count2));

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

        when(ledgerService.getBalance(eq(account), eq(pointInTime), eq(lookback)))
                .thenReturn(Mono.just(mockPrediction));

        // Act & Assert
        webTestClient.get().uri(uriBuilder -> uriBuilder
                        .path("/account/{account}/balance")
                        .queryParam("pointInTime", pointInTimeString)
                        .queryParam("lookbackDays", String.valueOf(lookbackDays))
                        .build(account))
                .exchange()
                .expectStatus().isOk()
                .expectBody(PredictionResult.class)
                .isEqualTo(mockPrediction);
    }


    @Test
    // lookback defaults to 537 days, pointInTime defaults to 21st June 2020
    void getBalance_withNoOptionalParams_shouldUseDefaultsAndReturnPredictionResult() {

        when(ledgerService.getBalance(eq(account), eq(PERMA_NOW), eq(Duration.ofDays(537))))
        .thenReturn(Mono.just(mockPrediction));
        
        webTestClient.get().uri("/account/{account}/balance", account)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PredictionResult.class)
                .isEqualTo(mockPrediction);
    }
}
