package com.matsnbelts.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public interface InvoiceGeneratorService {
    void generateInvoice(InputStream csvFileInputStream, File outputFolder,
                                final String invoiceDate, final String paymentDueDate, final String invoiceMonth)
            throws IOException, ParseException;
}
