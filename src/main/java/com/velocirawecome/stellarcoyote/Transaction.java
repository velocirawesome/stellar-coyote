package com.velocirawecome.stellarcoyote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("transactions")
public class Transaction {
    @Id
    private Long id;

    @Column("timestamp")
    private LocalDateTime timestamp;

    @Column("account")
    private String account;

    @Column("amount")
    private Double amount;
    
   // @Column("name")
    private String name;

    public Transaction(LocalDateTime timestamp, String account, Double amount, String name) {
        super();
        this.timestamp = timestamp;
        this.account = account;
        this.amount = amount;
        this.name = name;
    }

}
