package com.velocirawesome.stellarcoyote;

public class NoLedgerEntriesFoundException extends RuntimeException {
    public NoLedgerEntriesFoundException(String message) {
        super(message);
    }

    public NoLedgerEntriesFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
