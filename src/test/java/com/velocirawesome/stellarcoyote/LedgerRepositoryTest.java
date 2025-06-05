package com.velocirawesome.stellarcoyote;

import com.velocirawesome.stellarcoyote.LedgerEntry;
import com.velocirawesome.stellarcoyote.LedgerRepository;
import com.velocirawesome.stellarcoyote.PostgresContainerConfiguration;
import com.velocirawesome.stellarcoyote.StellarCoyoteApplication;
import com.velocirawesome.stellarcoyote.LedgerRepository.UserTransactionCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;

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

class LedgerRepositoryTest {

    @Autowired
    private LedgerRepository ledgerRepository;

    private LedgerEntry tx1, tx2, tx3;

    @BeforeEach
    void setUp() {
        tx1 = new LedgerEntry(LocalDateTime.now().minusDays(2), "acc1", 100.0, "user1", 100.0);
        tx2 = new LedgerEntry(LocalDateTime.now().minusDays(1), "acc1", 200.0, "user1", 300.0);
        tx3 = new LedgerEntry(LocalDateTime.now(), "acc2", 300.0, "user2", 300.0);
        ledgerRepository.deleteAll().block();
        ledgerRepository.save(tx1).block();
        ledgerRepository.save(tx2).block();
        ledgerRepository.save(tx3).block();
    }
    
    @Test
    public void testSaveAndFindAll() {
        List<LedgerEntry> allTransactions = ledgerRepository.findAll().collectList().block();
        assertThat(allTransactions)
            .hasSize(3)
            .extracting(LedgerEntry::getAccount, LedgerEntry::getAmount)
            .containsExactlyInAnyOrder(
                tuple("acc1", 100.0),
                tuple("acc1", 200.0),
                tuple("acc2", 300.0)
            );
    }

    @Test
    void testFindByAccountAndTimestampAfter() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1).minusHours(1);
        List<LedgerEntry> results = ledgerRepository.findByAccountAndTimestampAfter("acc1", cutoff).collectList().block();
        assertThat(results)
            .hasSize(1)
            .allMatch(tx -> tx.getAccount().equals("acc1") && tx.getAmount() == 200.0);

    }

    @Test
    public void testFindUserTransactionCounts() {
        Flux<LedgerRepository.UserTransactionCount> counts = ledgerRepository.findUserTransactionCounts();
        List<UserTransactionCount> results = counts.collectList().block();
        assertThat(results)
                .extracting(LedgerRepository.UserTransactionCount::getName, LedgerRepository.UserTransactionCount::getAccount, LedgerRepository.UserTransactionCount::getAmount)
                .containsExactlyInAnyOrder(
                    tuple("user1", "acc1", 2L),
                    tuple("user2", "acc2", 1L)
                );
            

    }

    @Test
    void testGetBalance() {
        given: /* tx1 and tx2 are for acc1, tx1 is 2 days ago, tx2 is 1 day ago */;
        when: /* get balance for acc1 up to now */;
        LocalDateTime upTo = LocalDateTime.now();
        Double balance = ledgerRepository.getBalance("acc1", upTo).block();
        expect: /* should sum of all transactions ie, tx1 + tx2 */;
        assertThat(balance).isEqualTo(300.0); 
    }

    @Test
    void testGetBalanceUpToFirstTransaction() {
        when: /* get balance for acc1 at a point in the past */;
        LocalDateTime upTo = tx1.getTimestamp();
        Double balance = ledgerRepository.getBalance("acc1", upTo).block();
        expect: /* should only include tx1 in total */;
         // tx1 is 100.0, tx2 is after this point
        assertThat(balance).isEqualTo(100.0); // Should only include tx1 for acc1
    }

    @Test
    void testGetBalanceNoTransactions() {
        when: /* get balance for acc1 at a point before any transactions */;
        LocalDateTime upTo = tx1.getTimestamp().minusDays(1);
        Double balance = ledgerRepository.getBalance("acc1", upTo).block();
        expect: /*  No transactions for acc1 before tx1, so balance should be null */;
        assertThat(balance).isNull();
    }

}