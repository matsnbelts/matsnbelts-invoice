package com.matsnbelts.controller;

import com.matsnbelts.exception.InvoiceException;
import com.matsnbelts.service.InvoiceGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

@RestController
@Slf4j
@RequestMapping("/admin")
public class SimpleController {

    @Autowired
    InvoiceGeneratorService invoiceGeneratorService;

    @CrossOrigin(origins = {"http://localhost:3000", "https://matsnbelts.firebaseapp.com"})
    @RequestMapping(value="/generate", method = RequestMethod.POST)
    public ResponseEntity<Object> generateInvoice(@RequestParam("file") MultipartFile fileUpload,
                                                  @RequestParam("output") String outputFolder,
                                                  @RequestParam("invoice-date") String invoiceDate,
                                                  @RequestParam("due-date") String dueDate,
                                                  @RequestParam("invoice-month") String invoiceMonth,
                                                  @RequestParam("send-mail") boolean sendMail) throws IOException, ParseException {
        log.info(fileUpload.getContentType());
        log.info("File Name {}", fileUpload.getOriginalFilename());
        BufferedReader br = new BufferedReader(new InputStreamReader(fileUpload.getInputStream()));
        log.info("Header {}", br.readLine());
        log.info("Output Folder passed {}", outputFolder);
        File outFolder = new File(outputFolder);
        assert(outFolder.exists());
        File[] invoiceFiles = outFolder.listFiles((dir, name) -> name.endsWith(".pdf"));
        if(!sendMail) {
            if (invoiceFiles != null && invoiceFiles.length > 0) {
                for (File file : invoiceFiles) {
                    file.delete();
                }
            }
        }
        try {
            invoiceGeneratorService.generateInvoice(fileUpload.getInputStream(), outFolder,
                invoiceDate, dueDate, invoiceMonth, sendMail);
            invoiceFiles = outFolder.listFiles((dir, name) -> name.endsWith(".pdf"));
        } catch (InvoiceException ie) {
            return new ResponseEntity<>(
                ie.getMessage(),
                HttpStatus.BAD_REQUEST
            );
        }
        return new ResponseEntity<>(
            String.valueOf(invoiceFiles != null ? invoiceFiles.length : 0),
            HttpStatus.OK
        );
    }
    }
