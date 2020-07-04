package com.matsnbelts.exception;

public class InvoiceException extends RuntimeException {
    public InvoiceException(Throwable cause) {
        super(cause);
    }
    public InvoiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
