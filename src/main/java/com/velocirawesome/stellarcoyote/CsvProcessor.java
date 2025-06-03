package com.velocirawesome.stellarcoyote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * utility to cut down the number of columns in the source csv. Reduces file
 * from 345 -> 5mb
 */
public class CsvProcessor {

    // only process every nth line but still samples from throughout the time range
    static int lineFraction = 10;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(
                    "Usage: java com.velocirawecome.stellarcoyote.CsvProcessor <inputFile.csv> <outputFile.csv>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            boolean negative = false;
            int row = -1; // Start at -1 to skip the header row
            Map<String, Double> accountTotals = new HashMap<>();

            String line;
            while ((line = reader.readLine()) != null) {
                List<String> columns = parseCsvLine(line);
                if (row++ % lineFraction != 0) {
                    continue; // Skip this line based on the fraction
                }
                List<String> selectedColumns = new ArrayList<>();
                selectedColumns.add(columns.get(1)); // date
                selectedColumns.add(columns.get(2)); // acc num
                selectedColumns.add((negative ? "-" : "") + columns.get(5)); // amount
                selectedColumns.add(columns.get(6) + " " + columns.get(7)); // first + last name

                // Calculate running total per account
                String account = columns.get(2);
                double amount = Double.parseDouble((negative ? "-" : "") + columns.get(5));
                double runningTotal = accountTotals.getOrDefault(account, 0.0) + amount;
                accountTotals.put(account, runningTotal);
                selectedColumns.add(String.valueOf(runningTotal)); // running total

                writer.write(String.join(",", selectedColumns));
                writer.newLine();
                negative = !negative; // Toggle negative for next line

            }
            System.out.println("Successfully processed CSV file. Output written to: " + outputFile);

        } catch (IOException e) {
            System.err.println("An error occurred during file processing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
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
        return fields;
    }
}
