package com.velocirawecome.stellarcoyote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** utility to cut down the number of columns in the source csv. Reduces file from 345 -> 53mb */
public class CsvProcessor {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java com.velocirawecome.stellarcoyote.CsvProcessor <inputFile.csv> <outputFile.csv>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                List<String> columns = parseCsvLine(line); // Basic CSV split, assumes no commas within fields
                
                if (columns.size() > 5) { // Ensure enough columns exist
                    List<String> selectedColumns = new ArrayList<>();
                    selectedColumns.add(columns.get(1)); // Column 1 (index 1)
                    selectedColumns.add(columns.get(2)); // Column 2 (index 2)
                    selectedColumns.add(columns.get(5)); // Column 5 (index 5)
                    
                    writer.write(String.join(",", selectedColumns));
                    writer.newLine();
                } else {
                    System.err.println("Skipping line due to insufficient columns: " + columns.size());
                }
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
