package com.matsnbelts.model;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

class Mailer{
    public static void send(String from,String password,String to,String sub,String msg, String filename, String cc){
        //Get properties object
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        //get Session
        Session session = Session.getDefaultInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("matsnbeltsapp@gmail.com","vsmatsnbelts");
                    }
                });
        //compose message
        try {
            MimeMessage message = new MimeMessage(session);
            message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
            message.addRecipient(Message.RecipientType.BCC,new InternetAddress("matsandbelts@gmail.com"));
            message.addRecipient(Message.RecipientType.BCC,new InternetAddress("matsnbeltsapp@gmail.com"));

            message.setSubject(sub);
            message.setText(msg);
            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setText(msg);
            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            //String filename = "/Users/srinis/Documents/MatsNBelts/test-invoice/9840736881.pdf";
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(new File(filename).getName());
            multipart.addBodyPart(messageBodyPart);

            // Send the complete message parts
            message.setContent(multipart);

            //send message
            Transport.send(message);
            System.out.println("message sent successfully");
        } catch (MessagingException e) {throw new RuntimeException(e);}

    }
}


public class SendMail
{
    public static void main(String [] args) {
        //"johnpraveen@yahoo.com"
        Mailer.send("matsnbeltsapp@gmail.com", "vsmatsnbelts", "srini.tvmalai11@gmail.com", "Mats And Belts - Invoice Generated for June'19",
                "Hey " + "Srinivas" +
                        ",\n This is an automatically generated email. Please do not reply to it.\n",
                "/Users/srinis/Documents/MatsNBelts/test-invoice/BP-03.pdf", "");
//        Mailer.send("matsnbeltsapp@gmail.com", "vsmatsnbelts",
//                "nitya.thomas@gmail.com", "Invoice - Mats N Belts", "This is an automatically generated email. Please do not reply to it.", );

    }
}  