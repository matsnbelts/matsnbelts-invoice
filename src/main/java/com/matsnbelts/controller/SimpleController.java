package com.matsnbelts.controller;

import com.matsnbelts.model.CustomerProfile;
import com.matsnbelts.model.InvoiceGenerator;
import com.matsnbelts.processor.LoadCustomerDetails;
import com.matsnbelts.service.InvoiceGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;
import java.text.ParseException;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/admin")
public class SimpleController {

    @Autowired
    InvoiceGeneratorService invoiceGeneratorService;

    @CrossOrigin(origins = {"http://localhost:3000", "https://matsnbelts.firebaseapp.com"})
    @RequestMapping(value="/generate", method = RequestMethod.POST)
    public String generateInvoice(@RequestParam("file") MultipartFile fileUpload,
                                  @RequestParam("output") String outputFolder,
                                  @RequestParam("invoice-date") String invoiceDate,
                                  @RequestParam("due-date") String dueDate,
                                  @RequestParam("invoice-month") String invoiceMonth) throws IOException, ParseException {
        log.info(fileUpload.getContentType());
        log.info("File Name {}", fileUpload.getOriginalFilename());
        BufferedReader br = new BufferedReader(new InputStreamReader(fileUpload.getInputStream()));
        log.info("Header {}", br.readLine());
        log.info("Output Folder passed {}", outputFolder);
        File outFolder = new File(outputFolder);
        assert(outFolder.exists());
        File[] invoiceFiles = outFolder.listFiles((dir, name) -> name.endsWith(".pdf"));
        if(invoiceFiles!= null && invoiceFiles.length > 0) {
            for(File file : invoiceFiles) {
                file.delete();
            }
        }

        invoiceGeneratorService.generateInvoice(fileUpload.getInputStream(), outFolder,
                invoiceDate, dueDate, invoiceMonth);
        invoiceFiles = outFolder.listFiles((dir, name) -> name.endsWith(".pdf"));
        return String.valueOf(invoiceFiles != null ? invoiceFiles.length : 0);
    }
}
