package com.matsnbelts.serviceImpl;

import com.matsnbelts.model.CustomerProfile;
import com.matsnbelts.model.InvoiceGenerator;
import com.matsnbelts.processor.LoadCustomerDetails;
import com.matsnbelts.processor.Mailer;
import com.matsnbelts.service.InvoiceGeneratorService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;

@Service
public class InvoiceGeneratorServiceImpl implements InvoiceGeneratorService {
    @Override
    public void generateInvoice(InputStream csvFileInputStream, File outputFolder,
                                final String invoiceDate, final String paymentDueDate,
                                final String invoiceMonth, final boolean sendMail)
            throws IOException, ParseException {
        LoadCustomerDetails loadCustomerDetails = new LoadCustomerDetails(invoiceMonth);
        Map<String, CustomerProfile> customerProfileMap = loadCustomerDetails
                .loadCustomersPaymentDetails(csvFileInputStream);

        for(Map.Entry<String, CustomerProfile> customerProfileEntry: customerProfileMap.entrySet()) {
            InvoiceGenerator.InvoiceGeneratorBuilder invoiceGeneratorBuilder = InvoiceGenerator.builder();
            InvoiceGenerator invoiceGenerator = invoiceGeneratorBuilder.customerProfile(customerProfileEntry.getValue())
                    .invoiceMonth(invoiceMonth)
                    .destinationFile(outputFolder.getAbsolutePath() + File.separator + customerProfileEntry.getValue().getCustomerId() + ".pdf").build();

            if(!sendMail) {
                invoiceGenerator.generateInvoicePdf(invoiceDate, paymentDueDate);
            } else if(Objects.requireNonNull(outputFolder.listFiles((dir, name) -> name.endsWith(".pdf"))).length > 0) {
                String from = "matsnbeltsapp@gmail.com";
                String pwd = "vsmatsnbelts";
                String to_mail = customerProfileEntry.getValue().getEmail();
                System.out.println("to::: " + to_mail);

                if(to_mail.isEmpty()) {
                    to_mail = "johnpraveen@yahoo.com";
                    System.out.println(":::: " + customerProfileEntry.getValue().getCustomerId());
                }
                Mailer.send(from, pwd, to_mail, "Mats And Belts - Invoice Generated for " + invoiceMonth + "'19",
                        "Hey " + customerProfileEntry.getValue().getCustomerName() +
                                ",\n This is an automatically generated email. Please do not reply to it.\n",
                        outputFolder.getAbsolutePath() + File.separator + customerProfileEntry.getValue().getCustomerId()+ ".pdf", "");
            }
        }
    }
}
