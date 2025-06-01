package com.velocirawecome.stellarcoyote;

import com.velocirawecome.stellarcoyote.TransactionRepository.UserTransactionCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataR2dbcTest
@ContextConfiguration(classes= {StellarCoyoteApplication.class, PostgresContainerConfiguration.class})

/**
 * notes:
 * Unable to retrieve @EnableAutoConfiguration base packages - add @ContextConfiguration
 * Query method expects at least 2 arguments but only found 1. - fix repo signature
 */

class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    private Transaction tx1, tx2, tx3;

    @BeforeEach
    void setUp() {
        tx1 = new Transaction(LocalDateTime.now().minusDays(2), "acc1", 100.0, "user1");
        tx2 = new Transaction(LocalDateTime.now().minusDays(1), "acc1", 200.0, "user1");
        tx3 = new Transaction(LocalDateTime.now(), "acc2", 300.0, "user2");
        transactionRepository.deleteAll().block();
        transactionRepository.save(tx1).block();
        transactionRepository.save(tx2).block();
        transactionRepository.save(tx3).block();
        System.out.println(transactionRepository.count().block());
    }
    
    @Test
    public void testSaveAndFindAll() {
        List<Transaction> allTransactions = transactionRepository.findAll().collectList().block();
        assertThat(allTransactions)
            .hasSize(3)
            .extracting(Transaction::getAccount, Transaction::getAmount)
            .containsExactlyInAnyOrder(
                tuple("acc1", 100.0),
                tuple("acc1", 200.0),
                tuple("acc2", 300.0)
            );
    }

    @Test
    void testFindByAccountAndTimestampAfter() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1).minusHours(1);
        List<Transaction> results = transactionRepository.findByAccountAndTimestampAfter("acc1", cutoff).collectList().block();
        assertThat(results)
            .hasSize(1)
            .allMatch(tx -> tx.getAccount().equals("acc1") && tx.getAmount() == 200.0);

    }

    @Test
    public void testFindUserTransactionCounts() {
        Flux<TransactionRepository.UserTransactionCount> counts = transactionRepository.findUserTransactionCounts();
        List<UserTransactionCount> results = counts.collectList().block();
        assertThat(results)
                .extracting(TransactionRepository.UserTransactionCount::getAccount, TransactionRepository.UserTransactionCount::getAmount)
                .containsExactlyInAnyOrder(
                    tuple("acc1", 2L),
                    tuple("acc2", 1L)
                );
            

    }
}
