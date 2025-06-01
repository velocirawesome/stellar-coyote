# stellar-coyote

echo testcontainers.reuse.enable=true > ~/.testcontainers.properties

jbang src/main/java/com/velocirawecome/stellarcoyote/CsvProcessor.java ~/Downloads/credit_card_transactions.csv 4_column.csv
