/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail.agent;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import net.thevpc.nmail.NMailAgent;
import net.thevpc.nmail.NMailBody;
import net.thevpc.nmail.NMailContext;
import net.thevpc.nmail.NMailMessage;
import net.thevpc.nmail.expr.ExprVars;
import net.thevpc.nmail.modules.NMailModuleProcessor;
import net.thevpc.nmail.util.NMailUtils;

/**
 * @author taha.bensalah@gmail.com
 */
public class DefaultNMailAgent implements NMailAgent {
    public static final NMailAgent INSTANCE = new DefaultNMailAgent();

    @Override
    public int sendMessage(NMailMessage mail, Properties roProperties, NMailContext mailContext, ExprVars vars)  {
        final Properties properties = new Properties();
        if (roProperties != null) {
            properties.putAll(roProperties);
        }
        if (mail.getProperties() != null) {
            properties.putAll(mail.getProperties());
        }
        Properties loggedProps = new Properties();
        for (String pr : new String[]{
                "mail.smtp.host",
                "mail.smtp.port",
                "app.mail.user",
                "app.mail.password",
                "mail.smtp.auth",
                "mail.smtp.starttls.enable"
        }) {
            if (properties.containsKey(pr)) {
                String v = properties.getProperty(pr);
                if (v != null) {
                    loggedProps.setProperty(pr, v);
                }
            }
        }

        //                putPropery(rwProperties, roProperties, , "true");
//                putPropery(rwProperties, roProperties, "mail.smtp.starttls.enable", "true");
//                putPropery(rwProperties, roProperties, "mail.smtp.host", "smtp.gmail.com");
//                putPropery(rwProperties, roProperties, "mail.smtp.port", "587");
        // Get the default Session object.
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.connectiontimeout", "10000"); // 10 seconds
        properties.put("mail.smtp.timeout", "10000");           // 10 seconds
        properties.put("mail.smtp.writetimeout", "10000");      // 10 seconds
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                String user = properties.getProperty("app.mail.user");
                String password = properties.getProperty("app.mail.password");
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(mail.from()));

            for (String v : mail.to()) {
                // Set To: header field of the header.
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(v));
            }
            for (String v : mail.cc()) {
                // Set To: header field of the header.
                message.addRecipient(Message.RecipientType.CC, new InternetAddress(v));
            }
            for (String v : mail.bcc()) {
                // Set To: header field of the header.
                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(v));
            }

            // Set Subject: header field
            message.setSubject(mail.subject());

            NMailBody mainBody = null;
            List<NMailBody> attachments = new ArrayList<>();
            for (NMailBody mm : mail.body()) {
                switch (mm.getPosition()) {
                    case OBJECT: {
                        if (mainBody == null) {
                            mainBody = mm;
                        } else {
                            throw new IllegalArgumentException("Only one main supported");
                        }
                        break;
                    }
                    case ATTACHMENT: {
                        attachments.add(mm);
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("not supported");
                    }
                }
            }
            if (attachments.isEmpty()) {
                message.setContent(getDataSource(mainBody, mailContext, vars), mainBody == null ? null : mainBody.getContentType());//
            } else {
                Multipart multipart = new MimeMultipart();
                int fileIndex = 0;
                multipart.addBodyPart(toMimeBodyPart(mainBody, fileIndex++, mailContext, vars));
                for (NMailBody xb : attachments) {
                    multipart.addBodyPart(toMimeBodyPart(xb, fileIndex++, mailContext, vars));
                }
                message.setContent(multipart);
            }
            message.setSentDate(new Date());
            if (mailContext.isLogMessage()) {
                // Now set the actual message
                System.out.println("sending message " + message);
                System.out.println("Using props ");
                for (Map.Entry<Object, Object> entry : loggedProps.entrySet()) {
                    System.out.println("\t " + entry.getKey() + " = " + entry.getValue());
                }
                System.out.println(mail);
            }
            if (mail.isDry()) {
                System.out.println("simulated sending " + mail.sourceId() + " message successful... ");
                //throw new MessagingException("error");
            } else {
                Transport.send(message);
                System.out.println("Sent " + mail.sourceId() + " message successfully... ");
            }
            return 1;
        } catch (MessagingException mex) {
            mex.printStackTrace();
            throw new UncheckedIOException(new IOException(mex));
        }
    }

    private Object getDataSource(NMailBody p, NMailContext expr, ExprVars vars)  {
        Object o = NMailModuleProcessor.resolveBodySource(p, expr, vars);
        if (o instanceof File) {
            return new FileDataSource((File) o);
        } else if (o instanceof URL) {
            if (NMailUtils.isTextPlainContentType(p.getContentType())) {
                return NMailModuleProcessor.loadBodyString(p, expr, vars);
            }
            if (NMailUtils.isTextHtmlContentType(p.getContentType())) {
                return NMailModuleProcessor.loadBodyStringHtml(p, expr, vars);
            }
            try {
                return new ByteArrayDataSource(NMailUtils.loadByteArray(((URL) o).openStream()), p.getContentType());
            }catch (IOException ex){
                throw new UncheckedIOException(ex);
            }
        } else if (o instanceof byte[]) {
            if (NMailUtils.isTextPlainContentType(p.getContentType())) {
                return NMailModuleProcessor.loadBodyString(p, expr, vars);
            }
            if (NMailUtils.isTextHtmlContentType(p.getContentType())) {
                return NMailModuleProcessor.loadBodyStringHtml(p, expr, vars);
            }
            return new ByteArrayDataSource(((byte[]) o), p.getContentType());
        } else {
            throw new IllegalArgumentException("Invalid");
        }
    }

    private MimeBodyPart toMimeBodyPart(NMailBody xb, int fileIndex, NMailContext expr, ExprVars vars) throws MessagingException {
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        Object dataSource = getDataSource(xb, expr, vars);
        messageBodyPart.setDataHandler(
                (dataSource instanceof DataSource) ? new DataHandler((DataSource) dataSource)
                        : new DataHandler(dataSource, xb.getContentType()));
        if (fileIndex > 0) {
            messageBodyPart.setHeader("Content-ID", "<" + ("part" + fileIndex) + ">");
        }
        Object o = NMailModuleProcessor.resolveBodySource(xb, expr, vars);
        if (o instanceof File) {
            messageBodyPart.setFileName(((File) o).getName());
        } else if (fileIndex > 0) {
            messageBodyPart.setFileName("part" + fileIndex);
        }
        return messageBodyPart;
    }
}
