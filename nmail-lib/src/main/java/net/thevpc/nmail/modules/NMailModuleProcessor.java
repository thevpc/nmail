/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail.modules;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import net.thevpc.nmail.*;
import net.thevpc.nmail.expr.ExprVars;
import net.thevpc.nmail.util.DefaultNMailContext;
import net.thevpc.nmail.util.NMailContextExprVars;
import net.thevpc.nmail.util.NMailUtils;
import net.thevpc.nmail.NMailDataSourceRow;

/**
 * @author taha.bensalah@gmail.com
 */
public class NMailModuleProcessor {

    private static NMailDataSourceRow row0 = new NMailDataSourceRow() {
        @Override
        public String rowId() {
            return "<null>";
        }

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
    private NMailAgent agent;
    private NMailConfig config;

    public NMailModuleProcessor(NMailAgent agent, NMailConfig config) {
        this.agent = agent == null ? DefaultNMailFactory.INSTANCE.createAgent() : agent;
        this.config = config == null ? new NMailConfig(null) : config;
    }

    public static byte[] loadBodyByteArray(NMailBody b, NMailContext expr, ExprVars vars)  {
        Object o = resolveBodySource(b, expr, vars);
        try {
            if (o instanceof File) {
                o = new FileInputStream((File) o);
            } else if (o instanceof URL) {
                o = ((URL) o).openStream();
            }
        }catch (IOException ex){
            throw new UncheckedIOException(ex);
        }
        if (o instanceof byte[]) {
            return ((byte[]) o);
        } else if (o instanceof InputStream) {
            return NMailUtils.loadByteArray((InputStream) o);
        } else {
            return (byte[]) o;
        }
    }

    //https://mateam.net/html-escape-characters/
    public static String loadBodyStringHtml(NMailBody b, NMailContext expr, ExprVars vars)  {
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

    public static String loadBodyString(NMailBody b, NMailContext expr, ExprVars vars)  {
        byte[] ba = loadBodyByteArray(b, expr, vars);
        return new String(ba);
    }

    public static Object resolveBodySource(NMailBody b, NMailContext expr, ExprVars vars)  {
        if (b instanceof NMailBodyContent) {
            return ((NMailBodyContent) b).getByteArray();
        } else if (b instanceof NMailBodyPath) {
            String pp = ((NMailBodyPath) b).getPath();
            String path = expr == null ? pp : expr.eval(pp, vars);
            File f = NMailUtils.toFileLenient(path);
            if (f != null) {
                return f;
            }
            try {
                return new URL(path);
            } catch (MalformedURLException e) {
                throw new UncheckedIOException(e);
            }
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

    public NMailAgent getAgent() {
        return agent;
    }

    public NMailModuleProcessor setAgent(NMailAgent agent) {
        this.agent = agent;
        return this;
    }

    public NMailConfig getConfig() {
        return config;
    }

    public void setConfig(NMailConfig config) {
        this.config = config;
    }

    public int sendMessage(NMail mail, Properties roProperties, final NMailListener listener) {
        if (roProperties == null) {
            roProperties = System.getProperties();
        }

        CounterNMailListener listener2 = new CounterNMailListener(listener);
        final NMailProperties roProperties0 = new DefaultProps(roProperties);
        if (mail.isExpandable()) {
            return sendExpandableMail(mail, roProperties, listener2);
        } else {
            NMailContext context = ctx(mail, roProperties0, row0);
            return sendMessage(new NMailMessage(mail), roProperties, context, new NMailContextExprVars(context, new HashMap<>()), listener2);
        }
//        return listener2.count;
    }

    private DefaultNMailContext ctx(NMail mail, NMailProperties properties, NMailDataSourceRow row) {
        return new DefaultNMailContext(mail.namedDataSources(), properties, row, mail.tracker(), true);
    }

    private int sendExpandableMail(final NMail mail, Properties roProperties, NMailListener listener) {

        final Properties rwProperties = new Properties();
        final Properties specialProperties = new Properties();
        rwProperties.putAll(mail.getProperties());
        prepareMailProperties(mail, rwProperties, roProperties);
        final NMailProperties props = new DefaultProps(rwProperties, roProperties, specialProperties);
        final NMailContext ctx = ctx(mail, props, row0);
        ExprVars vars = new NMailContextExprVars(ctx, new HashMap<>());
        int total = 0;
        if (mail.getCwd() != null) {
            vars.put("cwd", mail.getCwd());
        }
        for (NMailDataSource value : mail.namedDataSources().values()) {
            value.build(ctx, vars);
        }

        NMailDataSource repeatDataSource = mail.repeatDataSource();
        if (repeatDataSource != null) {
            repeatDataSource.build(ctx, vars);
        }
        if (repeatDataSource == null) {
            total += sendExpandableMailForRow(mail, row0, roProperties, listener, vars);
        } else {
            int maxRows = repeatDataSource.getRowCount();
            for (int i = 0; i < maxRows; i++) {
                NMailDataSourceRow r = repeatDataSource.getRow(i);
                total += sendExpandableMailForRow(mail, r, roProperties, listener, vars);
            }
        }
        return total;
    }

    private int sendExpandableMailForRow(final NMail mail, final NMailDataSourceRow r, final Properties roProperties, NMailListener listener, ExprVars vars) {
        int count = 0;
        final Properties rwProperties = new Properties();
        final Properties specialProperties = new Properties();
        rwProperties.putAll(mail.getProperties());
        prepareMailProperties(mail, rwProperties, roProperties);
        final NMailProperties props = new DefaultProps(rwProperties, roProperties, specialProperties);
        final NMailContext context = ctx(mail, props, r);
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
        String rowId = r.rowId();
        for (String to0 : cto) {
            boolean doSend = false;
            NMailMessage expandedMail = new NMailMessage(new NMail());
            String sourceId = rowId;
            if (!to0.isEmpty()) {
                sourceId += "/" + to0;
            }
            try {
                expandedMail.from(from);
                Set<String> to2 = new HashSet<>(to);
                to2.add(to0);
                String[] _to = prepareAddresses(to2, mail, props);
                String[] _cc = prepareAddresses(cc, mail, props);
                String[] _bcc = prepareAddresses(bcc, mail, props);
                Set<String> anyAddr = new HashSet<>();
                anyAddr.addAll(Arrays.asList(_to));
                if (anyAddr.isEmpty()) {
                    anyAddr.addAll(Arrays.asList(_cc));
                }
                if (anyAddr.isEmpty()) {
                    anyAddr.addAll(Arrays.asList(_bcc));
                }
                expandedMail.to(_to);
                expandedMail.cc(_cc);
                expandedMail.bcc(_bcc);
                expandedMail.subject(subject);
                sourceId += "/" + String.join(",", anyAddr);

                expandedMail.setSourceId(sourceId);
                expandedMail.getProperties().putAll(rwProperties);
                NMailBody mainBody = null;
                List<NMailBody> headers = new ArrayList<>();
                List<NMailBody> footers = new ArrayList<>();
                List<NMailBody> attachments = new ArrayList<>();
                List<NMailBody> allBodies2 = new ArrayList<>();

                for (NMailBody body : mail.body()) {

                    if (body.isExpandable()) {
                        if (body instanceof NMailBodyContent) {
                            NMailBodyContent bo = (NMailBodyContent) body;
                            NMailBodyContent bo2 = null;
                            String ct = bo.getContentType();
                            if (NMailUtils.isTextContentType(ct)) {
                                String s = new String(bo.getByteArray());
                                s = context.eval(s, vars);
                                bo2 = new NMailBodyContent(s.getBytes(), bo.getContentType(), false);
                            } else {
                                bo2 = new NMailBodyContent(bo.getByteArray(), bo.getContentType(), false);
                            }
                            bo2.setPosition(bo.getPosition());
                            bo2.setOrder(bo.getOrder());
                            allBodies2.add(bo2);
                        } else if (body instanceof NMailBodyPath) {
                            NMailBodyPath bo = (NMailBodyPath) body;
                            NMailBody bo2 = null;
                            String ct = context.eval(bo.getContentType(), vars);
                            String path = context.eval(bo.getPath(), vars);
                            if (NMailUtils.isTextContentType(ct)) {
                                BufferedReader br = new BufferedReader(new InputStreamReader(new URL(context.eval(bo.getPath(), vars)).openStream()));
                                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                                PrintStream out = new PrintStream(bout);
                                String line = null;
                                while ((line = br.readLine()) != null) {
                                    out.println(context.eval(line, vars));
                                }
                                out.flush();
                                bo2 = new NMailBodyContent(bout.toByteArray(), bo.getContentType(), false);
                                ((NMailBodyContent) bo2).setPosition(bo.getPosition());
                                ((NMailBodyContent) bo2).setOrder(bo.getOrder());
                            } else {
                                bo2 = new NMailBodyPath(path, ct, false);
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

                for (NMailBody mm : allBodies2) {
                    switch (mm.getPosition()) {
                        case HEADER: {
                            if (!NMailUtils.isTextContentType(mm.getContentType())) {
                                System.out.println("Header must be text or html");
                            }
                            headers.add(mm);
                            break;
                        }
                        case FOOTER: {
                            if (!NMailUtils.isTextContentType(mm.getContentType())) {
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
                                mainBody = new NMailBodyContent(newContent.getBytes(), mainBody.getContentType(), false);
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
                    mainBody = new NMailBodyContent(new byte[0], NMail.TEXT_CONTENT_TYPE, false);
                }
                if (NMailUtils.isTextHtmlContentType(mainBody.getContentType())) {
                    String b = loadBodyStringHtml(mainBody, context, vars);
                    for (int i = headers.size() - 1; i >= 0; i--) {
                        NMailBody header = headers.get(i);
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
                        NMailBody footer = footers.get(i);
                        b = insertBefore(b, "</body>", "\n" + loadBodyStringHtml(footer, context, vars) + "\n");
                    }
                    mainBody = new NMailBodyContent(b.getBytes(), mainBody.getContentType(), false);
                } else {
                    StringBuilder b = new StringBuilder(loadBodyString(mainBody, context, vars));
                    for (int i = headers.size() - 1; i >= 0; i--) {
                        NMailBody header = headers.get(i);
                        b.insert(0, loadBodyString(header, context, vars) + "\n");
                    }
                    for (int i = footers.size() - 1; i >= 0; i--) {
                        NMailBody footer = footers.get(i);
                        b.append("\n").append(loadBodyString(footer, context, vars));
                    }
                    mainBody = new NMailBodyContent(b.toString().getBytes(), mainBody.getContentType(), false);
                }
                expandedMail.body().add(mainBody);
                for (NMailBody x : attachments) {
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

    private String[] prepareAddresses(Set<String> addresses, NMail mail, NMailProperties props) {
        Set<String> addresses2 = new HashSet<>();
        for (String a : addresses) {
            if (a != null) {
                a = a.trim();
                if (a.length() > 0) {
                    for (InternetAddress b : expandAddresses(a, mail, props)) {
                        addresses2.add(b.toString());
                    }
                }
            }
        }
        return addresses2.toArray(new String[addresses2.size()]);
    }

    private void putProperty(Properties properties, Properties roProperties, String name, String value) {
        if (!properties.containsKey(name) && !roProperties.contains(name)) {
            properties.setProperty(name, value);
        }
    }

    private void prepareMailProperties(NMail mail, Properties rwProperties, Properties roProperties) {
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
                    putProperty(rwProperties, roProperties, (String) (entry.getKey()), (String) (entry.getValue()));
                }
            }
        }
    }

    private int sendMessage(NMailMessage mail, Properties roProperties, NMailContext expr, ExprVars vars, NMailListener listener) {
        String rowId = mail.sourceId();
        NTracker tracker = expr.tracker();
        if (tracker != null) {
            try {
                NTracker.Status old = tracker.getRowStatus(rowId);
                if (old == NTracker.Status.SUCCESS) {
                    //already done!
                    return 0;
                }
                tracker.setRowStatus(rowId, NTracker.Status.TODO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (listener != null) {
            listener.onBeforeSend(mail);
        }
        int count = 0;
        Throwable error = null;
        try {
            count = splitMessagesThenSendEach(mail, roProperties, expr, vars);
            if (tracker != null) {
                try {
                    tracker.setRowStatus(rowId, NTracker.Status.SUCCESS);
                } catch (Exception e) {
                    //
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
            error = th;
            if (tracker != null) {
                try {
                    tracker.setRowStatus(rowId, NTracker.Status.ERROR);
                } catch (Exception e) {
                    //
                }
            }
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

    private int splitMessagesThenSendEach(NMailMessage mail, Properties roProperties, NMailContext mailContext, ExprVars vars) {
        int max=-1;
        final Properties properties = new Properties();
        if (roProperties != null) {
            properties.putAll(roProperties);
        }
        if (mail.getProperties() != null) {
            properties.putAll(mail.getProperties());
        }

        String maxRecipients = properties.getProperty("nmail.max-recipients");
        if(maxRecipients!=null){
            max=Integer.parseInt(maxRecipients);
        }
        if(max<=0){
            return agent.sendMessage(mail, roProperties, mailContext, vars);
        }
        int recipients = mail.to().size() + mail.bcc().size() + mail.cc().size();
        if(recipients >max){
            List<NMailMessage> splittedMails=new ArrayList<>();
            LinkedList<NMailRecipient> recipientsList=new LinkedList<>();
            int count=0;
            for (String s : mail.to()) {
                recipientsList.add(new NMailRecipient(NMailRecipientType.TO,s));
            }
            for (String s : mail.cc()) {
                recipientsList.add(new NMailRecipient(NMailRecipientType.CC,s));
            }
            for (String s : mail.bcc()) {
                recipientsList.add(new NMailRecipient(NMailRecipientType.BCC,s));
            }

            NMailMessage m0= mail.copy();
            m0.cc().clear();
            m0.to().clear();
            m0.cc().clear();
            m0.bcc().clear();

            while(!recipientsList.isEmpty()){
                count=max;
                NMailMessage m2 = m0.copy();
                while(!recipientsList.isEmpty() && count>0){
                    count--;
                    NMailRecipient recipient = recipientsList.removeFirst();
                    m2.addRecipients(recipient.getType(),recipient.getValue());
                }
                splittedMails.add(m2);
            }
            int x=0;
            for (NMailMessage other : splittedMails) {
                x+=agent.sendMessage(other, roProperties, mailContext, vars);
            }
            return x;
        }else{
            return agent.sendMessage(mail, roProperties, mailContext, vars);
        }
    }


    private List<InternetAddress> expandAddresses(String address, NMail m, NMailProperties props) {
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

    private class CounterNMailListener implements NMailListener {

        int count = 0;
        NMailListener listener;

        public CounterNMailListener(NMailListener listener) {
            this.listener = listener;
        }

        @Override
        public void onBeforeSend(NMailMessage mail) {
            if (listener != null) {
                listener.onBeforeSend(mail);
            }
        }

        @Override
        public void onAfterSend(NMailMessage mail) {
            if (listener != null) {
                listener.onAfterSend(mail);
            }
            count++;
//            System.out.println("Sent " + count);
        }

        @Override
        public void onSendError(NMailMessage mail, Throwable exc) {
            if (listener != null) {
                listener.onSendError(mail, exc);
            }
        }

    }
}
