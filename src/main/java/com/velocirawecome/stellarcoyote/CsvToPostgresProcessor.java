package com.velocirawecome.stellarcoyote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvToPostgresProcessor implements CommandLineRunner {
    private final TransactionRepository transactionRepository;

    @Autowired
    public CsvToPostgresProcessor(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        String csvFile = "3_column.csv";
        List<Transaction> transactions = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Adjust as needed

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] columns = parseCsvLine(line);
                if (columns.length < 3) continue;
                LocalDateTime timestamp = LocalDateTime.parse(columns[0].trim(), formatter);
                String account = columns[1].trim();
                Double amount = Double.parseDouble(columns[2].trim());
                transactions.add(new Transaction(timestamp, account, amount));
            }
        }

        Flux<Transaction> transactionFlux = Flux.fromIterable(transactions);
        transactionRepository.saveAll(transactionFlux)
            .doOnComplete(() -> System.out.println("All transactions saved to Postgres."))
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
