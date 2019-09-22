package com.matsnbelts.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public interface InvoiceGeneratorService {
    void generateInvoice(InputStream csvFileInputStream, String outputFolder,
                                final String invoiceDate, final String paymentDueDate)
            throws IOException, ParseException;
}
