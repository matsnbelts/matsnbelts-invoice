package com.matsnbelts.model;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.matsnbelts.processor.LoadCustomerDetails;
import lombok.Builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Builder
public class InvoiceGenerator {
    public static final String DEST = "simple_rowspan_colspan.pdf";
    private String destinationFile;
    private CustomerProfile customerProfile;
    private String area;
    private String cityPincode;
    private String vehicle = "Car";

    public void generateInvoicePdf(final String invoiceDate, final String paymentDueDate) throws IOException {
        if(area == null) {
            area = "";
        }
        if(cityPincode == null) {
            cityPincode = "Chennai";
        }
        createPdf(destinationFile, invoiceDate, paymentDueDate);
    }

    private void createPdf(String dest, final String invoiceDate, final String paymentDueDate) throws IOException {
        FileOutputStream fos = new FileOutputStream(dest);
        PdfWriter writer = new PdfWriter(fos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        addInvoiceHeader(document); //add invoice head title
        setCustomerDetailsLayout(document); // set customer account details
        float[] col_widths = {40, 40};
        Table table = new Table(col_widths);
        table.setWidth(UnitValue.createPointValue(200));
        table.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        DeviceRgb lightGrey = new DeviceRgb(227, 225, 225);
        table.addCell(getCell(1, "Customer Id", lightGrey, TextAlignment.LEFT));
        table.addCell(getCell(1, customerProfile.getCustomerId(), null, TextAlignment.RIGHT));

        table.addCell(getCell(1, "Invoice #",lightGrey, TextAlignment.LEFT));
        table.addCell(getCell(1, "MNB-19-" +  System.currentTimeMillis(), null, TextAlignment.RIGHT));

        table.addCell(getCell(1, "Invoice Date", lightGrey, TextAlignment.LEFT));
        table.addCell(getCell(1, invoiceDate, null, TextAlignment.RIGHT));

        table.addCell(getCell(1, "Payment Due Date", lightGrey, TextAlignment.LEFT));
        table.addCell(getCell(1, paymentDueDate, null, TextAlignment.RIGHT));
        document.add(table);
        setUnitsLayout(document);
        setFooterLayout(document);
        document.close();

    }

    private void addInvoiceHeader(Document document) throws IOException {
        Paragraph para = new Paragraph("I N V O I C E");
        para.setTextAlignment(TextAlignment.CENTER);
        para.setBackgroundColor(Color.BLACK);
        para.setFontColor(Color.WHITE);
        PdfFont bold = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);
        para.setFont(bold);
        para.setFontSize(12);
        document.add(para);
    }

    private void setUnitsLayout(Document document) throws IOException {
        //Description,CarNo,CarModel,CarType,Period,Quantity,Unit,Price
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 1, 2, 1, 2, 2}));
        table.setMarginTop(20);
        setUnitsHeader(table);
        populateCarsTableData(table);
        document.add(table);
    }

    private void addPromoCodeRows(Table table, final String description, final String discount) throws IOException {
        Paragraph matsnbeltspara = new Paragraph(description);

        matsnbeltspara.setTextAlignment(TextAlignment.LEFT);
        Cell cell = new Cell(1, 6).add(matsnbeltspara);
        table.addCell(cell);
        ///// promocode
        cell = new Cell(1, 1).add(getUnitsColumnParagraph("(" +discount + ")", TextAlignment.RIGHT));
        table.addCell(cell);
        cell = new Cell(1, 1).add(getUnitsColumnParagraph("(" +discount + ")", TextAlignment.RIGHT));
        table.addCell(cell);
    }

    private void populateCarsTableData(Table table) throws IOException {
        Cell cell;
        int i = 1;
        double totalAmount = 0;
        Map<String, String> promoCodeMap = new LinkedHashMap<String, String>();
        for(CustomerCar car : customerProfile.getCars()) {
            final String vehicle = (car.getCarType().equalsIgnoreCase(LoadCustomerDetails.Pack.BIKE))
                    ? LoadCustomerDetails.Pack.BIKE : "Car";
            cell = new Cell(1, 1).add(
                    getUnitsColumnParagraph(vehicle + " Subscription " + i, TextAlignment.LEFT));
            table.addCell(cell);
            cell = new Cell(1, 1).add(
                    getUnitsColumnParagraph(car.getCarNo(), TextAlignment.LEFT));
            table.addCell(cell);
            cell = new Cell(1, 1).add(
                    getUnitsColumnParagraph(car.getCarModel(), TextAlignment.LEFT));
            table.addCell(cell);
            cell = new Cell(1, 1).add(
                    getUnitsColumnParagraph(car.getCarType(), TextAlignment.LEFT));
            table.addCell(cell);
            cell = new Cell(1, 1).add(
                    getUnitsColumnParagraph(car.getStartDate() + " - " + "31/08/2019", TextAlignment.LEFT));
            table.addCell(cell);
            cell = new Cell(1, 1).add(
                    getUnitsColumnParagraph("1", TextAlignment.RIGHT));
            table.addCell(cell);
            double discount = car.getActualRate() - car.getDiscountRate();
            if(discount > 0) {
                if(customerProfile.getCars().size() == 1) {
                    promoCodeMap.put("Discount for car - " + car.getPromoCode(),
                            String.format("%.02f", discount));
                } else {
                    promoCodeMap.put("Discount for " + i + getDayOfMonthSuffix(i) + " car - " + car.getPromoCode(),
                            String.format("%.02f", discount));
                }
            }
            totalAmount += car.getActualRate();
            totalAmount -= discount;
            cell = new Cell(1, 1).add(
                    getUnitsColumnParagraph(String.format("%.02f", car.getActualRate()), TextAlignment.RIGHT));
            table.addCell(cell);
            cell = new Cell(1, 1).add(
                    getUnitsColumnParagraph(String.format("%.02f", car.getActualRate()), TextAlignment.RIGHT));
            table.addCell(cell);
            i++;
        }
        customerProfile.setTotalInvoiceAmount(totalAmount);
        for(Map.Entry<String, String> promo:promoCodeMap.entrySet()) {

            addPromoCodeRows(table, promo.getKey(), promo.getValue());
        }

        Paragraph matsnbeltspara = new Paragraph("Thanks for your business!");
        PdfFont bold = PdfFontFactory.createFont(FontConstants.TIMES_ITALIC);
        matsnbeltspara.setFont(bold);
        matsnbeltspara.setMarginTop(20);
        matsnbeltspara.setFontSize(8);
        matsnbeltspara.setFontColor(Color.BLUE);

        matsnbeltspara.setTextAlignment(TextAlignment.CENTER);
        cell = new Cell(2, 5).add(matsnbeltspara);
        table.addCell(cell);
        ///// subtotal
        cell = new Cell(1, 2).add(getUnitsColumnParagraph("Subtotal:", TextAlignment.LEFT));
        cell.setBorderRight(Border.NO_BORDER);
        table.addCell(cell);
        cell = new Cell(1, 1).add(
                getUnitsColumnParagraph(String.format("%.02f", totalAmount), TextAlignment.RIGHT));
        cell.setBorderLeft(Border.NO_BORDER);
        table.addCell(cell);
        ////// total
        ///// subtotal
        cell = new Cell(1, 2).add(getUnitsColumnParagraph("Total:", TextAlignment.LEFT));
        cell.setBorderRight(Border.NO_BORDER);
        table.addCell(cell);
        cell = new Cell(1, 1).add(
                getUnitsColumnParagraph(String.format("%.02f", totalAmount), TextAlignment.RIGHT));
        cell.setBorderLeft(Border.NO_BORDER);
        table.addCell(cell);
    }

    private String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }

    private Paragraph getUnitsColumnParagraph(String text, TextAlignment alignment) {
        Paragraph para = new Paragraph(text);
        para.setTextAlignment(alignment);
        return para;
    }

    private void setUnitsHeader(Table table) {
        Cell cell;
        cell = new Cell(1, 1).add(getUnitsHeaderParagraph("Description"));
        DeviceRgb lightGrey = new DeviceRgb(227, 225, 225);
        cell.setBackgroundColor(lightGrey, 0.5f);
        table.addCell(cell);
        cell = new Cell(1, 1).add(getUnitsHeaderParagraph("Vehicle No."));
        cell.setBackgroundColor(lightGrey, 0.5f);
        table.addCell(cell);
        cell = new Cell(1, 1).add(getUnitsHeaderParagraph("Vehicle Model"));
        cell.setBackgroundColor(lightGrey, 0.5f);
        table.addCell(cell);
        cell = new Cell(1, 1).add(getUnitsHeaderParagraph("Type"));
        cell.setBackgroundColor(lightGrey, 0.5f);
        table.addCell(cell);
        cell = new Cell(1, 1).add(getUnitsHeaderParagraph("Period"));
        cell.setBackgroundColor(lightGrey, 0.5f);
        table.addCell(cell);
        cell = new Cell(1, 1).add(getUnitsHeaderParagraph("Qty."));
        cell.setBackgroundColor(lightGrey, 0.5f);
        table.addCell(cell);
        cell = new Cell(1, 1).add(getUnitsHeaderParagraph("Unit"));
        cell.setBackgroundColor(lightGrey, 0.5f);
        table.addCell(cell);
        cell = new Cell(1, 1).add(getUnitsHeaderParagraph("Price"));
        cell.setBackgroundColor(lightGrey, 0.5f);
        table.addCell(cell);
    }

    private Paragraph getUnitsHeaderParagraph(String text) {
        Paragraph para = new Paragraph(text);
        para.setBold();
        para.setTextAlignment(TextAlignment.CENTER);
        return para;
    }

    private void setFooterLayout(Document document) {
        Paragraph para = new Paragraph("");
        para.setTextAlignment(TextAlignment.CENTER);
        para.setFontSize(11);
        document.add(para);
        para = new Paragraph("If you have any questions about this invoice, please contact");
        para.setTextAlignment(TextAlignment.CENTER);
        para.setFontSize(11);
        document.add(para);
        para = new Paragraph("9840736881");
        para.setTextAlignment(TextAlignment.CENTER);
        para.setFontSize(11);
        document.add(para);
    }

    private void setCustomerDetailsLayout(Document document) throws IOException {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        table.setKeepTogether(true);
        table.setBorder(Border.NO_BORDER);
        Cell cell;
        cell = new Cell(1, 1).add(new Paragraph(customerProfile.getCustomerName()));
        cell.setBorder(Border.NO_BORDER);
        table.addCell(cell);

        Paragraph matsnbeltspara = new Paragraph("MATS N BELTS");
        PdfFont bold = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);
        matsnbeltspara.setFont(bold);
        matsnbeltspara.setTextAlignment(TextAlignment.CENTER);
        matsnbeltspara.setFontColor(new DeviceRgb(219, 59, 50));
        matsnbeltspara.setFontSize(16);
        matsnbeltspara.setUnderline();
        cell = new Cell(6, 1).add(matsnbeltspara);
        cell.setMarginTop(40);
        cell.setBorder(Border.NO_BORDER);

        table.addCell(cell);
        String divider = customerProfile.getApartmentNo() == null || customerProfile.getApartmentNo().isEmpty() ? "" : ", ";
        cell = new Cell(1, 1).add(new Paragraph(
                customerProfile.getApartmentNo() + divider + customerProfile.getApartment()));
        cell.setBorder(Border.NO_BORDER);

        table.addCell(cell);
        cell = new Cell(1, 1).add(new Paragraph(area));
        cell.setBorder(Border.NO_BORDER);

        table.addCell(cell);
        cell = new Cell(1, 1).add(new Paragraph(cityPincode));
        cell.setBorder(Border.NO_BORDER);

        table.addCell(cell);
        cell = new Cell(1, 1).add(new Paragraph(customerProfile.getMobile()));
        cell.setBorder(Border.NO_BORDER);

        table.addCell(cell);
        Paragraph mailPara = new Paragraph(customerProfile.getEmail());
        mailPara.setUnderline();
        mailPara.setFontColor(Color.BLUE);
        cell = new Cell(1, 1).add(mailPara);
        cell.setBorder(Border.NO_BORDER);

        table.addCell(cell);

        document.add(table);

    }

    private Cell getCell(int colspan, String text, Color bgColor, TextAlignment textAlignment) {
        Cell cell = new Cell(1, colspan);
        Paragraph p = new Paragraph(text);
        //p.setFontSize(8);
        if(bgColor != null) {
            cell.setBackgroundColor(bgColor, 0.5f);
        }
        p.setTextAlignment(textAlignment);
        cell.add(p);
        return cell;
    }

    public void generateConsolidatedReport(String fileName) throws IOException {
        FileWriter csvWriter = new FileWriter(new File(fileName), true);
        final String comma = ",";
        final String newline = "\n";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(customerProfile.getCustomerId()).append(comma).append(
                customerProfile.getCustomerName()).append(comma).append(customerProfile.getEmail()
        ).append(comma).append(customerProfile.getMobile()).append(comma).append(customerProfile.getApartmentNo())
                .append(comma).append(customerProfile.getApartment()).append(comma).append(customerProfile.getTotalInvoiceAmount()).append(newline);
        csvWriter.append(stringBuilder.toString());
        csvWriter.close();
    }
}
