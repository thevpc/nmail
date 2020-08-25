/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail;

import net.vpc.common.gomail.modules.GoMailModuleSerializer;
import java.io.File;
import java.io.Reader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import net.vpc.common.io.FileUtils;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class GoMail implements Serializable, Cloneable {

    public static final String TEXT_CONTENT_TYPE = "text/plain;charset=UTF-8";
    public static final String HTML_CONTENT_TYPE = "text/html;charset=UTF-8";
    public static final String BYTES_CONTENT_TYPE = "application/octet-stream";

    private boolean simulate = false;
    private String subject;
    private String from;
    private GoMailBodyList body = new GoMailBodyList();
    private Properties properties = new Properties();
    private Set<String> to = new HashSet<>();
    private Set<String> cc = new HashSet<>();
    private Set<String> bcc = new HashSet<>();
    private Set<String> cto = new HashSet<>();
    private GoMailDataSource repeatDataSource;
    private Map<String, GoMailDataSource> namedDataSources = new HashMap<>();
    private boolean expandable = true;

    public static GoMail load(Reader reader) {
        return new GoMailModuleSerializer().read(reader);
    }

    public static GoMail load(File file) {
        if (file.getName().endsWith("gomail")) {
            return new GoMailModuleSerializer().read(GoMailFormat.TEXT, file);
        }
        throw new IllegalArgumentException("Unsupported file " + file.getAbsolutePath() + ". accepted extensions are *.gomail");
    }

    public GoMail copy() {
        try {
            GoMail o = (GoMail) clone();
            o.properties = new Properties(properties);
            o.body = body.copy();
            o.to = new HashSet(to);
            o.cc = new HashSet(cc);
            o.bcc = new HashSet(bcc);
            o.cto = new HashSet(cto);
            o.namedDataSources = new HashMap<>(namedDataSources);
            return o;
        } catch (CloneNotSupportedException ex) {
            throw new IllegalArgumentException("Never");
        }
    }

    public boolean isSimulate() {
        return simulate;
    }

    public GoMail setSimulate(boolean simulate) {
        this.simulate = simulate;
        return this;
    }

    public GoMail body(Object value, String type) {
        return body(value, type, GoMailBodyPosition.OBJECT);
    }

    public GoMail header(Object value, String type) {
        return body(value, type, GoMailBodyPosition.HEADER);
    }

    public GoMail attachment(Object value, String type) {
        return body(value, type, GoMailBodyPosition.ATTACHMENT);
    }

    public GoMail footer(Object value, String type) {
        return body(value, type, GoMailBodyPosition.FOOTER);
    }

    public GoMail body(Object value, String contentType, GoMailBodyPosition position) {
        AbstractGoMailBody theBody = null;
        if (value != null) {
            if (value instanceof String) {
                if (contentType == null) {
                    contentType = TEXT_CONTENT_TYPE;
                }
                theBody = new GoMailBodyContent(((String) value).getBytes(), contentType, true);
            } else if (value instanceof CharSequence) {
                if (contentType == null) {
                    contentType = TEXT_CONTENT_TYPE;
                }
                String ss = ((CharSequence) value).toString();
                theBody = new GoMailBodyContent(ss.getBytes(), contentType, true);
            } else if (value instanceof byte[]) {
                if (contentType == null) {
                    contentType = GoMail.BYTES_CONTENT_TYPE;
                }
                theBody = (new GoMailBodyContent(((byte[]) value), contentType, true));
            } else if (value instanceof File) {
                File file = (File) value;
                if (contentType == null) {
                    contentType = FileUtils.probeContentType(file);
                }
                try {
                    theBody = (new GoMailBodyPath((file).toURI().toURL().toString(), contentType, true));
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
            } else if (value instanceof URL) {
                URL file = (URL) value;

                if (contentType == null) {
                    contentType = FileUtils.probeContentType(file);
                }
                theBody = (new GoMailBodyPath((file).toString(), contentType, true));
            } else {
                throw new RuntimeException("Unsupported type " + (value == null ? "null" : value.getClass().getName()));
            }
            theBody.setPosition(position);
            body.add(theBody);
        }
        return this;
    }

    public GoMailBodyList body() {
        return body;
    }

    public String subject() {
        return subject;
    }

    public GoMail subject(String subject) {
        this.subject = subject;
        return this;
    }

    public String from() {
        return from;
    }

    public GoMail from(String from) {
        this.from = from;
        return this;
    }

    public GoMail setTo(Set<String> s) {
        if (s == null) {
            this.to = new HashSet<>();
        } else {
            this.to = new HashSet<>(s);
        }
        return this;
    }

    public GoMail setCc(Set<String> s) {
        if (s == null) {
            this.cc = new HashSet<>();
        } else {
            this.cc = new HashSet<>(s);
        }
        return this;
    }

    public GoMail setBcc(Set<String> s) {
        if (s == null) {
            this.bcc = new HashSet<>();
        } else {
            this.bcc = new HashSet<>(s);
        }
        return this;
    }

    public Set<String> getTo() {
        return to();
    }

    public Set<String> to() {
        return to;
    }

    public Set<String> getCc() {
        return cc();
    }

    public Set<String> cc() {
        return cc;
    }

    public Set<String> getBcc() {
        return bcc();
    }

    public Set<String> bcc() {
        return bcc;
    }

    public List<Recipient> getRecipients() {
        return recipients();
    }

    public List<Recipient> recipients() {
        List<Recipient> all = new ArrayList<>();
        for (String s : to) {
            all.add(new Recipient(RecipientType.TO, s));
        }
        for (String s : cto) {
            all.add(new Recipient(RecipientType.TOEACH, s));
        }
        for (String s : cc) {
            all.add(new Recipient(RecipientType.CC, s));
        }
        for (String s : bcc) {
            all.add(new Recipient(RecipientType.BCC, s));
        }
        return all;
    }

    public GoMail addRecipients(Collection<Recipient> recipients) {
        for (Recipient recipient : recipients) {
            addRecipient(recipient);
        }
        return this;
    }

    public GoMail addRecipient(Recipient recipient) {
        return addRecipient(recipient.getType(), recipient.getValue());
    }

    public GoMail addRecipient(RecipientType type, String recipient) {
        switch (type) {
            case TO: {
                to(recipient);
                break;
            }
            case CC: {
                cc(recipient);
                break;
            }
            case BCC: {
                bcc(recipient);
                break;
            }
            case TOEACH: {
                toeach(recipient);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported recipientType " + type);
            }
        }
        return this;
    }

    public GoMail addRecipients(RecipientType recipientType, String... others) {
        switch (recipientType) {
            case TO: {
                to(others);
                break;
            }
            case TOEACH: {
                toeach(others);
                break;
            }
            case CC: {
                cc(others);
                break;
            }
            case BCC: {
                bcc(others);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported recipientType " + recipientType);
            }
        }
        return this;
    }

    public GoMail setRecipients(RecipientType recipientType, String... others) {
        return setRecipients(recipientType, Arrays.asList(others));
    }

    public GoMail setRecipients(RecipientType recipientType, Collection<String> others) {
        switch (recipientType) {
            case TO: {
                to().clear();
                to(others);
                break;
            }
            case CC: {
                cc().clear();
                cc(others);
                break;
            }
            case BCC: {
                bcc().clear();
                bcc(others);
                break;
            }
            case TOEACH: {
                toeach().clear();
                toeach(others);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported recipientType " + recipientType);
            }
        }
        return this;
    }

    public Set<String> getRecipients(RecipientType recipientType) {
        switch (recipientType) {
            case TO: {
                return to();
            }
            case CC: {
                return cc();
            }
            case BCC: {
                return bcc();
            }
            case TOEACH: {
                return toeach();
            }
        }
        throw new IllegalArgumentException("Unsupported recipientType " + recipientType);
    }

    public GoMail to(String... others) {
        to.addAll(Arrays.asList(others));
        return this;
    }

    public GoMail cc(String... others) {
        cc.addAll(Arrays.asList(others));
        return this;
    }

    public GoMail bcc(String... others) {
        bcc.addAll(Arrays.asList(others));
        return this;
    }

    public GoMail to(Collection<String> others) {
        to.addAll(others);
        return this;
    }

    public GoMail cc(Collection<String> others) {
        cc.addAll(others);
        return this;
    }

    public GoMail bcc(Collection<String> others) {
        bcc.addAll(others);
        return this;
    }

    public GoMail setProperty(String name, String value) {
        if (name != null) {
            if (value == null) {
                getProperties().remove(name);
            } else {
                getProperties().setProperty(name, value);
            }
        }
        return this;
   }

    public Properties getProperties() {
        return properties;
    }

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    public GoMail toeach(Collection<String> others) {
        cto.addAll(others);
        return this;
    }

    public GoMail setToEach(Set<String> s) {
        if (s == null) {
            this.cto = new HashSet<>();
        } else {
            this.cto = new HashSet<>(s);
        }
        return this;
    }

    public Set<String> toeach() {
        return cto;
    }

    public GoMail toeach(String... others) {
        cto.addAll(Arrays.asList(others));
        return this;
    }

    public Map<String, GoMailDataSource> namedDataSources() {
        return namedDataSources;
    }

    public GoMailDataSource repeatDataSource() {
        return repeatDataSource;
    }

    public GoMail repeatDatasource(GoMailDataSource dataSource) {
        this.repeatDataSource = dataSource;
        return this;
    }

    public GoMail setCredentials(String user, String password) {
        if (user != null && user.length() == 0) {
            user = null;
        }
        if (password != null && password.length() == 0) {
            password = null;
        }
        setProperty("app.mail.user", user);
        setProperty("app.mail.password", password);
        return this;
    }

    @Override
    public String toString() {
        GoMailModuleSerializer s = new GoMailModuleSerializer();
        return s.gomailToString(this);
//        return XMailFormatter.format(this);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.simulate ? 1 : 0);
        hash = 89 * hash + Objects.hashCode(this.subject);
        hash = 89 * hash + Objects.hashCode(this.from);
        hash = 89 * hash + Objects.hashCode(this.body);
        hash = 89 * hash + Objects.hashCode(this.properties);
        hash = 89 * hash + Objects.hashCode(this.to);
        hash = 89 * hash + Objects.hashCode(this.cc);
        hash = 89 * hash + Objects.hashCode(this.bcc);
        hash = 89 * hash + Objects.hashCode(this.repeatDataSource);
        hash = 89 * hash + (this.expandable ? 1 : 0);
        hash = 89 * hash + Objects.hashCode(this.cto);
        hash = 89 * hash + Objects.hashCode(this.namedDataSources);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GoMail other = (GoMail) obj;
        if (this.simulate != other.simulate) {
            return false;
        }
        if (!Objects.equals(this.subject, other.subject)) {
            return false;
        }
        if (!Objects.equals(this.from, other.from)) {
            return false;
        }
        if (!Objects.equals(this.body, other.body)) {
            return false;
        }
        if (!Objects.equals(this.properties, other.properties)) {
            return false;
        }
        if (!Objects.equals(this.to, other.to)) {
            return false;
        }
        if (!Objects.equals(this.cc, other.cc)) {
            return false;
        }
        if (!Objects.equals(this.bcc, other.bcc)) {
            return false;
        }
        if (!Objects.equals(this.repeatDataSource, other.repeatDataSource)) {
            return false;
        }
        if (!Objects.equals(this.namedDataSources, other.namedDataSources)) {
            return false;
        }
        if (this.expandable != other.expandable) {
            return false;
        }
        if (!Objects.equals(this.cto, other.cto)) {
            return false;
        }
        return true;
    }

    public void send() {
        send(null);
    }

    public void send(GoMailListener listener) {
        DefaultGoMailFactory.INSTANCE.createProcessor().sendMessage(this, null, listener);
    }
}
