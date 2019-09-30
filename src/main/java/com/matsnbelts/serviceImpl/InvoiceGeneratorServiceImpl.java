package com.matsnbelts.serviceImpl;

import com.matsnbelts.model.CustomerProfile;
import com.matsnbelts.model.InvoiceGenerator;
import com.matsnbelts.processor.LoadCustomerDetails;
import com.matsnbelts.service.InvoiceGeneratorService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;

@Service
public class InvoiceGeneratorServiceImpl implements InvoiceGeneratorService {
    @Override
    public void generateInvoice(InputStream csvFileInputStream, File outputFolder,
                                final String invoiceDate, final String paymentDueDate, final String invoiceMonth)
            throws IOException, ParseException {
        LoadCustomerDetails loadCustomerDetails = new LoadCustomerDetails(invoiceMonth);
        Map<String, CustomerProfile> customerProfileMap = loadCustomerDetails
                .loadCustomersPaymentDetails(csvFileInputStream);

        for(Map.Entry<String, CustomerProfile> customerProfileEntry: customerProfileMap.entrySet()) {
            InvoiceGenerator.InvoiceGeneratorBuilder invoiceGeneratorBuilder = InvoiceGenerator.builder();
            InvoiceGenerator invoiceGenerator = invoiceGeneratorBuilder.customerProfile(customerProfileEntry.getValue())
                    .invoiceMonth(invoiceMonth)
                    .destinationFile(outputFolder.getAbsolutePath() + File.separator + customerProfileEntry.getValue().getCustomerId() + ".pdf").build();
            invoiceGenerator.generateInvoicePdf(invoiceDate, paymentDueDate);
            //invoiceGenerator.generateConsolidatedReport(fileName);
        }
    }
}
