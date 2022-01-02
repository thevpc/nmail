/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.modules;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import net.thevpc.gomail.*;
import net.thevpc.gomail.util.DefaultGoMailContext;
import net.thevpc.gomail.util.GoMailUtils;
import net.thevpc.gomail.GoMailDataSourceRow;

/**
 * @author taha.bensalah@gmail.com
 */
public class GoMailModuleProcessor {

    private static GoMailDataSourceRow row0 = new GoMailDataSourceRow() {

        @Override
        public String get(int index) {
            return null;
        }

        @Override
        public String get(String name) {
            return null;
        }

        @Override
        public String[] getColumns() {
            return new String[0];
        }
    };
    private GoMailAgent agent;
    private GoMailConfig config;

    //    public GoMailModuleProcessor(GoMailAgent agent, ClassLoader loader,GoMailFactory factory) throws IOException {
//        this.agent = agent == null ? factory.createAgent(): agent;
//        this.config = new GoMailConfig(loader);
//    }
    public GoMailModuleProcessor(GoMailAgent agent, GoMailConfig config) {
        this.agent = agent == null ? DefaultGoMailFactory.INSTANCE.createAgent() : agent;
        this.config = config == null ? new GoMailConfig(null) : config;
    }

    //    private InputStream getInputStream(String path) throws IOException {
//        return new URL(path).openStream();
//    }
    public static byte[] loadBodyByteArray(GoMailBody b, GoMailContext expr, Map<String, Object> vars) throws IOException {
        Object o = resolveBodySource(b, expr, vars);
        if (o instanceof File) {
            o = new FileInputStream((File) o);
        } else if (o instanceof URL) {
            o = ((URL) o).openStream();
        }
        if (o instanceof byte[]) {
            return ((byte[]) o);
        } else if (o instanceof InputStream) {
            return GoMailUtils.loadByteArray((InputStream) o);
        } else {
            return (byte[]) o;
        }
    }

    //https://mateam.net/html-escape-characters/
    public static String loadBodyStringHtml(GoMailBody b, GoMailContext expr, Map<String, Object> vars) throws IOException {
        String s = loadBodyString(b, expr, vars);
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case 'œ': {
                    sb.append("&oelig;");
                    break;
                }
                case 'æ': {
                    sb.append("&aelig;");
                    break;
                }
                case 'è': {
                    sb.append("&egrave;");
                    break;
                }
                case 'é': {
                    sb.append("&eacute;");
                    break;
                }
                case 'ê': {
                    sb.append("&ecirc;");
                    break;
                }
                case 'ë': {
                    sb.append("&euml;");
                    break;
                }
                case 'ç': {
                    sb.append("&ccedil;");
                    break;
                }
                case 'ô': {
                    sb.append("&ocirc;");
                    break;
                }
                case 'õ': {
                    sb.append("&otilde;");
                    break;
                }
                case 'ö': {
                    sb.append("&ouml;");
                    break;
                }
                case 'ù': {
                    sb.append("&ugrave;");
                    break;
                }
                case 'ú': {
                    sb.append("&uacute;");
                    break;
                }
                case 'û': {
                    sb.append("&ucirc;");
                    break;
                }
                case 'ü': {
                    sb.append("&uuml;");
                    break;
                }
                case 'î': {
                    sb.append("&icirc;");
                    break;
                }
                case 'ï': {
                    sb.append("&iuml;");
                    break;
                }
                case 'à': {
                    sb.append("&agrave;");
                    break;
                }
                case 'â': {
                    sb.append("&acirc;");
                    break;
                }
                case 'ä': {
                    sb.append("&auml;");
                    break;
                }
                case 'ÿ': {
                    sb.append("&yuml;");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static String loadBodyString(GoMailBody b, GoMailContext expr, Map<String, Object> vars) throws IOException {
        byte[] ba = loadBodyByteArray(b, expr, vars);
        return new String(ba);
    }

    public static Object resolveBodySource(GoMailBody b, GoMailContext expr, Map<String, Object> vars) throws IOException {
        if (b instanceof GoMailBodyContent) {
            return ((GoMailBodyContent) b).getByteArray();
        } else if (b instanceof GoMailBodyPath) {
            String pp = ((GoMailBodyPath) b).getPath();
            String path = expr == null ? pp : expr.eval(pp, vars);
            File f = GoMailUtils.toFileLenient(path);
            if (f != null) {
                return f;
            }
            return new URL(path);
        } else {
            throw new IllegalArgumentException("Unsupported");
        }
    }

    private static String insertAfter(String baseStr, String key, String inserted) {
        int x = baseStr.toLowerCase().indexOf(key.toLowerCase());
        if (x >= 0) {
            StringBuilder s = new StringBuilder();
            s.append(baseStr.substring(0, x + key.length()));
            s.append(inserted);
            s.append(baseStr.substring(x + key.length()));
            return s.toString();
        } else {
            return inserted + baseStr;
        }
    }

    private static String insertBefore(String baseStr, String key, String inserted) {
        int x = baseStr.toLowerCase().indexOf(key.toLowerCase());
        if (x >= 0) {
            StringBuilder s = new StringBuilder();
            s.append(baseStr.substring(0, x));
            s.append(inserted);
            s.append(baseStr.substring(x));
            return s.toString();
        } else {
            return baseStr + inserted;
        }
    }

    //    public GoMailModuleProcessor() throws IOException {
//        this(null, (GoMailConfig) null);
//    }
    public GoMailAgent getAgent() {
        return agent;
    }

    public void setAgent(GoMailAgent agent) {
        this.agent = agent;
    }

    public GoMailConfig getConfig() {
        return config;
    }

    public void setConfig(GoMailConfig config) {
        this.config = config;
    }

    public int sendMessage(GoMail mail, Properties roProperties, final GoMailListener listener) {
        if (roProperties == null) {
            roProperties = System.getProperties();
        }

        CounterGoMailListener listener2 = new CounterGoMailListener(listener);
        final GoMailProperties roProperties0 = new DefaultProps(roProperties);
        if (mail.isExpandable()) {
            return sendExpandableMail(mail, roProperties, listener2);
        } else {
            GoMailContext context = new DefaultGoMailContext(mail.namedDataSources(), roProperties0, row0);
            Map<String, Object> vars = new HashMap<>();
            return sendMessage(new GoMailMessage(mail), roProperties, context, vars, listener2);
        }
//        return listener2.count;
    }

    private int sendExpandableMail(final GoMail mail, Properties roProperties, GoMailListener listener) {

        final Properties rwProperties = new Properties();
        final Properties specialProperties = new Properties();
        rwProperties.putAll(mail.getProperties());
        prepareMailProperties(mail, rwProperties, roProperties);
        final GoMailProperties props = new DefaultProps(rwProperties, roProperties, specialProperties);
        final GoMailContext ctx = new DefaultGoMailContext(mail.namedDataSources(), props, row0);
        Map<String, Object> vars = new HashMap<>();
        int total = 0;
        if (mail.getCwd() != null) {
            vars.put("cwd", mail.getCwd());
        }
        for (GoMailDataSource value : mail.namedDataSources().values()) {
            value.build(ctx, vars);
        }

        GoMailDataSource repeatDataSource = mail.repeatDataSource();
        if (repeatDataSource != null) {
            repeatDataSource.build(ctx, vars);
        }
        if (repeatDataSource == null) {
            total += sendExpandableMailForRow(mail, row0, roProperties, listener, vars);
        } else {
            int maxRows = repeatDataSource.getRowCount();
            for (int i = 0; i < maxRows; i++) {
                GoMailDataSourceRow r = repeatDataSource.getRow(i);
                total += sendExpandableMailForRow(mail, r, roProperties, listener, vars);
            }
        }
        return total;
    }

    private int sendExpandableMailForRow(final GoMail mail, final GoMailDataSourceRow r, final Properties roProperties, GoMailListener listener, Map<String, Object> vars) {
        int count = 0;
        final Properties rwProperties = new Properties();
        final Properties specialProperties = new Properties();
        rwProperties.putAll(mail.getProperties());
        prepareMailProperties(mail, rwProperties, roProperties);
        final GoMailProperties props = new DefaultProps(rwProperties, roProperties, specialProperties);
        final GoMailContext context = new DefaultGoMailContext(mail.namedDataSources(), props, r);
        String from0 = mail.from();
        if (from0 == null || from0.trim().isEmpty() && props.getProperty("app.mail.user") != null) {
            from0 = props.getProperty("app.mail.user");
        }
        String from = context.eval(from0, vars);
        specialProperties.setProperty("from", from);

        Set<String> to = new HashSet<>();
        for (String t : mail.to()) {
            to.add(context.eval(t, vars));
        }
        Set<String> cc = new HashSet<>();
        for (String t : mail.cc()) {
            cc.add(context.eval(t, vars));
        }
        Set<String> bcc = new HashSet<>();
        for (String t : mail.bcc()) {
            bcc.add(context.eval(t, vars));
        }
        Set<String> cto = new HashSet<>();
        for (String t : mail.toeach()) {
            cto.add(context.eval(t, vars));
        }
        if (cto.isEmpty()) {
            cto.add("");
        }
        String subject = context.eval(mail.subject(), vars);
        for (String to0 : cto) {
            boolean doSend = false;
            GoMailMessage expandedMail = new GoMailMessage(new GoMail());
            try {
                expandedMail.from(from);
                Set<String> to2 = new HashSet<>(to);
                to2.add(to0);
                expandedMail.to(prepareAddresses(to2, mail, props));
                expandedMail.cc(prepareAddresses(cc, mail, props));
                expandedMail.bcc(prepareAddresses(bcc, mail, props));
                expandedMail.subject(subject);

                expandedMail.getProperties().putAll(rwProperties);
                GoMailBody mainBody = null;
                List<GoMailBody> headers = new ArrayList<>();
                List<GoMailBody> footers = new ArrayList<>();
                List<GoMailBody> attachments = new ArrayList<>();
                List<GoMailBody> allBodies2 = new ArrayList<>();

                for (GoMailBody body : mail.body()) {

                    if (body.isExpandable()) {
                        if (body instanceof GoMailBodyContent) {
                            GoMailBodyContent bo = (GoMailBodyContent) body;
                            GoMailBodyContent bo2 = null;
                            String ct = bo.getContentType();
                            if (GoMailUtils.isTextContentType(ct)) {
                                String s = new String(bo.getByteArray());
                                s = context.eval(s, vars);
                                bo2 = new GoMailBodyContent(s.getBytes(), bo.getContentType(), false);
                            } else {
                                bo2 = new GoMailBodyContent(bo.getByteArray(), bo.getContentType(), false);
                            }
                            bo2.setPosition(bo.getPosition());
                            bo2.setOrder(bo.getOrder());
                            allBodies2.add(bo2);
                        } else if (body instanceof GoMailBodyPath) {
                            GoMailBodyPath bo = (GoMailBodyPath) body;
                            GoMailBody bo2 = null;
                            String ct = context.eval(bo.getContentType(), vars);
                            String path = context.eval(bo.getPath(), vars);
                            if (GoMailUtils.isTextContentType(ct)) {
                                BufferedReader br = new BufferedReader(new InputStreamReader(new URL(context.eval(bo.getPath(), vars)).openStream()));
                                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                                PrintStream out = new PrintStream(bout);
                                String line = null;
                                while ((line = br.readLine()) != null) {
                                    out.println(context.eval(line, vars));
                                }
                                out.flush();
                                bo2 = new GoMailBodyContent(bout.toByteArray(), bo.getContentType(), false);
                                ((GoMailBodyContent) bo2).setPosition(bo.getPosition());
                                ((GoMailBodyContent) bo2).setOrder(bo.getOrder());
                            } else {
                                bo2 = new GoMailBodyPath(path, ct, false);
                                bo2.setPosition(bo.getPosition());
                                bo2.setOrder(bo.getOrder());
                            }
                            allBodies2.add(bo2);
                        } else {

                        }
                    } else {
                        allBodies2.add(body);
                    }
                }

                for (GoMailBody mm : allBodies2) {
                    switch (mm.getPosition()) {
                        case HEADER: {
                            if (!GoMailUtils.isTextContentType(mm.getContentType())) {
                                System.out.println("Header must be text or html");
                            }
                            headers.add(mm);
                            break;
                        }
                        case FOOTER: {
                            if (!GoMailUtils.isTextContentType(mm.getContentType())) {
                                System.out.println("Footer must be text or html");
                            }
                            footers.add(mm);
                            break;
                        }
                        case OBJECT: {
                            if (mainBody == null) {
                                mainBody = mm;
                            } else {
                                String newContent = loadBodyString(mainBody, context, vars) + "\n" + loadBodyString(mm, context, vars);
                                mainBody = new GoMailBodyContent(newContent.getBytes(), mainBody.getContentType(), false);
                            }
                            break;
                        }
                        case ATTACHMENT: {
                            attachments.add(mm);
                            break;
                        }
                    }
                }
                if (mainBody == null) {
                    mainBody = new GoMailBodyContent(new byte[0], GoMail.TEXT_CONTENT_TYPE, false);
                }
                if (GoMailUtils.isTextHtmlContentType(mainBody.getContentType())) {
                    String b = loadBodyStringHtml(mainBody, context, vars);
                    for (int i = headers.size() - 1; i >= 0; i--) {
                        GoMailBody header = headers.get(i);
                        int x = b.toLowerCase().indexOf("<body>");
                        if (x >= 0) {
                            StringBuilder s = new StringBuilder();
                            if (x > 0) {
                                s.append(b.substring(0, x + "<body>".length()));
                            }
                            s.append(loadBodyStringHtml(header, context, vars));
                            s.append("\n");
                            s.append(b.substring(x + "<body>".length()));
                        } else {

                        }
                        b = insertAfter(b, "<body>", "\n" + loadBodyStringHtml(header, context, vars) + "\n");
                    }
                    for (int i = footers.size() - 1; i >= 0; i--) {
                        GoMailBody footer = footers.get(i);
                        b = insertBefore(b, "</body>", "\n" + loadBodyStringHtml(footer, context, vars) + "\n");
                    }
                    mainBody = new GoMailBodyContent(b.getBytes(), mainBody.getContentType(), false);
                } else {
                    StringBuilder b = new StringBuilder(loadBodyString(mainBody, context, vars));
                    for (int i = headers.size() - 1; i >= 0; i--) {
                        GoMailBody header = headers.get(i);
                        b.insert(0, loadBodyString(header, context, vars) + "\n");
                    }
                    for (int i = footers.size() - 1; i >= 0; i--) {
                        GoMailBody footer = footers.get(i);
                        b.append("\n").append(loadBodyString(footer, context, vars));
                    }
                    mainBody = new GoMailBodyContent(b.toString().getBytes(), mainBody.getContentType(), false);
                }
                expandedMail.body().add(mainBody);
                for (GoMailBody x : attachments) {
                    expandedMail.body().add(x);
                }
                expandedMail.setDry(mail.isDry());

                if ((expandedMail.to() != null && expandedMail.to().size() > 0)
                        || (expandedMail.cc() != null && expandedMail.cc().size() > 0)
                        || (expandedMail.bcc() != null && expandedMail.bcc().size() > 0)) {
                    doSend = true;
                }
            } catch (Exception error) {
                doSend = false;
                if (listener != null) {
                    listener.onSendError(expandedMail, error);
                }
            }
            if (doSend) {
                count += sendMessage(expandedMail, roProperties, context, vars, listener);
            }
        }
        return count;
    }

    private String[] prepareAddresses(Set<String> addresses, GoMail mail, GoMailProperties props) {
        Set<String> addresses2 = new HashSet<>();
        for (String a : addresses) {
            if (a != null) {
                a = a.trim();
                if (a.length() > 0) {
                    for (InternetAddress b : expanAddresses(a, mail, props)) {
                        addresses2.add(b.toString());
                    }
                }
            }
        }
        return addresses2.toArray(new String[addresses2.size()]);
    }

    private void putPropery(Properties properties, Properties roProperties, String name, String value) {
        if (!properties.containsKey(name) && !roProperties.contains(name)) {
            properties.setProperty(name, value);
        }
    }

    private void prepareMailProperties(GoMail mail, Properties rwProperties, Properties roProperties) {
        String from = mail.from();
        if (from == null && mail.getProperties().getProperty("app.mail.user") != null) {
            from = mail.getProperties().getProperty("app.mail.user");
        }
        String provider = mail.provider();
        //if(properties.contains(mail))
        if (from != null) {
            Properties props = config.findConfig(from, provider);
            if (props != null) {
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    putPropery(rwProperties, roProperties, (String) (entry.getKey()), (String) (entry.getValue()));
                }
            }
        }
    }

    private int sendMessage(GoMailMessage mail, Properties roProperties, GoMailContext expr, Map<String, Object> vars, GoMailListener listener) {
        if (listener != null) {
            listener.onBeforeSend(mail);
        }
        int count = 0;
        Throwable error = null;
        try {
            count = agent.sendMessage(mail, roProperties, expr, vars);
        } catch (Throwable th) {
            th.printStackTrace();
            error = th;
        }
        if (listener != null) {
            if (error == null) {
                listener.onAfterSend(mail);
            } else {
                listener.onSendError(mail, error);
            }
        }
        return count;
    }

    private List<InternetAddress> expanAddresses(String address, GoMail m, GoMailProperties props) {
        List<InternetAddress> all = new ArrayList<>();
        if (address != null) {
            address = address.trim();
            if (address.length() > 0) {
                try {
                    InternetAddress[] arr = InternetAddress.parse(address, false);
                    for (InternetAddress a : arr) {
                        if (a.getAddress().equals("${from}")) {
                            if (props.getProperty("from") == null) {
                                all.add(new InternetAddress(m.from()));
                            } else {
                                all.add(new InternetAddress(props.getProperty("from")));
                            }
                        } else {
                            all.add(a);
                        }
                    }
                } catch (AddressException ex) {
                    throw new IllegalArgumentException(ex);
                }
            }
        }
        return all;
    }

    private class CounterGoMailListener implements GoMailListener {

        int count = 0;
        GoMailListener listener;

        public CounterGoMailListener(GoMailListener listener) {
            this.listener = listener;
        }

        @Override
        public void onBeforeSend(GoMailMessage mail) {
            if (listener != null) {
                listener.onBeforeSend(mail);
            }
        }

        @Override
        public void onAfterSend(GoMailMessage mail) {
            if (listener != null) {
                listener.onAfterSend(mail);
            }
            count++;
//            System.out.println("Sent " + count);
        }

        @Override
        public void onSendError(GoMailMessage mail, Throwable exc) {
            if (listener != null) {
                listener.onSendError(mail, exc);
            }
        }

    }
}
