package com.velocirawesome.stellarcoyote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("ledger")
public class LedgerEntry {
    @Id
    private Long id;

    @Column("timestamp")
    private LocalDateTime timestamp;

    @Column("account")
    private String account;

    @Column("amount")
    private Double amount;
    
    @Column("name")
    private String name;

    @Column("running_total")
    private Double total;

    
    public LedgerEntry(LocalDateTime timestamp, String account, Double amount, String name, Double total) {
        super();
        this.timestamp = timestamp;
        this.account = account;
        this.amount = amount;
        this.name = name;
        this.total = total;
    }

}
