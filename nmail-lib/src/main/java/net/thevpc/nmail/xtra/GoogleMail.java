package net.thevpc.nmail.xtra;

///*
// * To change this license header, choose License Headers in Project Properties.
// *
// * and open the template in the editor.
// */
//package net.thevpc.lib.nmail.xtra;
//
//import com.sun.mail.smtp.SMTPTransport;
//import java.io.File;
//import java.net.URL;
//import java.security.Security;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//import java.util.Properties;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.mail.Message;
//import javax.mail.MessagingException;
//import javax.mail.Multipart;
//import javax.mail.Session;
//import javax.mail.internet.AddressException;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;
//
///**
// *
// * @author taha.bensalah@gmail.com
// */
//public class GoogleMail {
//    /**
//     * Send email using GMail SMTP server.
//     *
//     * @param username GMail username
//     * @param password GMail password
//     * @param recipientEmail TO recipient
//     * @param title title of the message
//     * @param messageText message to be sent
//     * @throws AddressException if the email address parse failed
//     * @throws MessagingException if the connection is dead or not in the connected state or if the message is not a MimeMessage
//     */
//    public static void Send(final String username, final String password, String recipientEmail, String title, String messageText, String messageHtml, List<URL> messageHtmlInline, List<URL> attachments) throws AddressException, MessagingException {
//        GoogleMail.Send(username, password, recipientEmail, "", title, messageText, messageHtml, messageHtmlInline,attachments);
//    }
//
//    /**
//     * Send email using GMail SMTP server.
//     *
//     * @param username GMail username
//     * @param password GMail password
//     * @param recipientEmail TO recipient
//     * @param ccEmail CC recipient. Can be empty if there is no CC recipient
//     * @param title title of the message
//     * @param messageText message to be sent
//     * @throws AddressException if the email address parse failed
//     * @throws MessagingException if the connection is dead or not in the connected state or if the message is not a MimeMessage
//     */
//    public static void Send(final String username, final String password, String recipientEmail, String ccEmail, String title, String messageText, String messageHtml, List<URL> messageHtmlInline, List<URL> attachments) throws AddressException, MessagingException {
//        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
//        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
//
//        // Get a Properties object
//        Properties props = System.getProperties();
//        props.setProperty("mail.smtps.host", "smtp.gmail.com");
//        props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
//        props.setProperty("mail.smtp.socketFactory.fallback", "false");
//        props.setProperty("mail.smtp.port", "465");
//        props.setProperty("mail.smtp.socketFactory.port", "465");
//        props.setProperty("mail.smtps.auth", "true");
//
//        /*
//        If set to false, the QUIT command is sent and the connection is immediately closed. If set
//        to true (the default), causes the transport to wait for the response to the QUIT command.
//
//        ref :   http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html
//                http://forum.java.sun.com/thread.jspa?threadID=5205249
//                smtpsend.java - demo program from javamail
//        */
//        props.put("mail.smtps.quitwait", "false");
//
//        Session session = Session.getInstance(props, null);
//
//        // -- Create a new message --
//        final MimeMessage msg = new MimeMessage(session);
//
//        // -- Set the FROM and TO fields --
//        msg.setFrom(new InternetAddress(username + "@gmail.com"));
//        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail, false));
//
//        if (ccEmail.length() > 0) {
//            msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail, false));
//        }
//
//        msg.setSubject(title);
//
//        // mixed
//        MailContentBuilder mailContentBuilder = new MailContentBuilder();
//        final Multipart mpMixed = mailContentBuilder.build(messageText, messageHtml, messageHtmlInline, attachments);
//        msg.setContent(mpMixed);
//        msg.setSentDate(new Date());
//
//        SMTPTransport t = (SMTPTransport)session.getTransport("smtps");
//
//        t.connect("smtp.gmail.com", username, password);
//        t.sendMessage(msg, msg.getAllRecipients());
//        t.close();
//    }
//}
