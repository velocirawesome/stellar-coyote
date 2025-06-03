package com.velocirawesome.stellarcoyote;

import com.velocirawesome.stellarcoyote.LedgerRepository.UserTransactionCount;
import lombok.Data;

// Mock UserTransactionCount since it's an interface
@Data
class MockUserTransactionCount implements UserTransactionCount {
    private String name;
    private Long amount;
    private String account;
    
    public MockUserTransactionCount(String name, Long amount, String account) {
        this.name = name;
        this.amount = amount;
        this.account = account;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Long getAmount() {
        return amount;
    }

    @Override
    public String getAccount() {
        return account;
    }
}