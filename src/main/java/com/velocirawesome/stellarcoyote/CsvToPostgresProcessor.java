package com.velocirawesome.stellarcoyote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CsvToPostgresProcessor {
    private final LedgerRepository transactionRepository;

    int batches = 0;

    public CsvToPostgresProcessor(LedgerRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void run() throws Exception {
        if (transactionRepository.count().block() > 0) {
            log.info("Database already populated, skipping CSV import.");
            return;
        }
        String csvFile = "4_column.csv";
        List<LedgerEntry> transactions = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] columns = parseCsvLine(line);
                if (columns.length < 5)
                    continue;
                LocalDateTime timestamp = LocalDateTime.parse(columns[0].trim(), formatter);
                String account = columns[1].trim();
                Double amount = Double.parseDouble(columns[2].trim());
                String name = columns[3].trim();
                Double total = Double.parseDouble(columns[4].trim());
                transactions.add(new LedgerEntry(timestamp, account, amount, name, total));
            }
        }

        Flux<LedgerEntry> transactionFlux = Flux.fromIterable(transactions);

        transactionFlux
        .window(1000)
        .doOnNext(_ -> {
            batches += 1000;
            log.info("Processing batch, {} rows completed", batches);
        }).flatMap(transactionRepository::saveAll, 4)
        .doOnError(ex -> log.error("Error processing transactions from {}.", csvFile, ex))
        .blockLast();
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }
}
