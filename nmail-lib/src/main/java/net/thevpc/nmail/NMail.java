/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail;

import net.thevpc.nmail.modules.NMailModuleSerializer;

import java.io.File;
import java.io.Reader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import net.thevpc.nmail.util.NMailUtils;
import net.thevpc.nuts.io.NPath;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class NMail implements Serializable, Cloneable {

    public static final String TEXT_CONTENT_TYPE = "text/plain;charset=UTF-8";
    public static final String HTML_CONTENT_TYPE = "text/html;charset=UTF-8";
    public static final String BYTES_CONTENT_TYPE = "application/octet-stream";

    private boolean dry = false;
    private String cwd;
    private String subject;
    private String from;
    private String provider;
    private NTracker tracker;
    private NMailBodyList body = new NMailBodyList();
    private Properties properties = new Properties();
    private Set<String> to = new HashSet<>();
    private Set<String> cc = new HashSet<>();
    private Set<String> bcc = new HashSet<>();
    private Set<String> cto = new HashSet<>();
    private NMailDataSource repeatDataSource;
    private Map<String, NMailDataSource> namedDataSources = new HashMap<>();
    private boolean expandable = true;

    public static NMail load(Reader reader) {
        return new NMailModuleSerializer().read(reader);
    }

    public static NMail load(File file) {
        return new NMailModuleSerializer().read(NPath.of(file));
    }

    public NMail copy() {
        try {
            NMail o = (NMail) clone();
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

    public boolean isDry() {
        return dry;
    }

    public NMail setDry(boolean dry) {
        this.dry = dry;
        return this;
    }

    public NMail body(Object value, String type) {
        return body(value, type, NMailBodyPosition.OBJECT);
    }

    public NMail header(Object value, String type) {
        return body(value, type, NMailBodyPosition.HEADER);
    }

    public NMail attachment(Object value, String type) {
        return body(value, type, NMailBodyPosition.ATTACHMENT);
    }

    public NMail footer(Object value, String type) {
        return body(value, type, NMailBodyPosition.FOOTER);
    }

    public NMail body(Object value, String contentType, NMailBodyPosition position) {
        AbstractNMailBody theBody = null;
        if (value != null) {
            if (value instanceof String) {
                if (contentType == null) {
                    contentType = TEXT_CONTENT_TYPE;
                }
                theBody = new NMailBodyContent(((String) value).getBytes(), contentType, true);
            } else if (value instanceof CharSequence) {
                if (contentType == null) {
                    contentType = TEXT_CONTENT_TYPE;
                }
                String ss = ((CharSequence) value).toString();
                theBody = new NMailBodyContent(ss.getBytes(), contentType, true);
            } else if (value instanceof byte[]) {
                if (contentType == null) {
                    contentType = NMail.BYTES_CONTENT_TYPE;
                }
                theBody = (new NMailBodyContent(((byte[]) value), contentType, true));
            } else if (value instanceof File) {
                File file = (File) value;
                if (contentType == null) {
                    contentType = NMailUtils.probeContentType(file.getName());
                }
                try {
                    theBody = (new NMailBodyPath((file).toURI().toURL().toString(), contentType, true));
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }
            } else if (value instanceof URL) {
                URL file = (URL) value;

                if (contentType == null) {
                    contentType = NMailUtils.probeContentType(file.toString());
                }
                theBody = (new NMailBodyPath((file).toString(), contentType, true));
            } else {
                throw new RuntimeException("Unsupported type " + (value == null ? "null" : value.getClass().getName()));
            }
            theBody.setPosition(position);
            body.add(theBody);
        }
        return this;
    }

    public NMailBodyList body() {
        return body;
    }

    public String subject() {
        return subject;
    }

    public NMail subject(String subject) {
        this.subject = subject;
        return this;
    }

    public String from() {
        return from;
    }

    public NMail from(String from) {
        this.from = from;
        return this;
    }

    public NMail tracker(NTracker tracking) {
        this.tracker = tracking;
        return this;
    }

    public String provider() {
        return provider;
    }

    public NTracker tracker() {
        return tracker;
    }

    public NMail provider(String provider) {
        this.provider = provider;
        return this;
    }

    public NMail setTo(Set<String> s) {
        if (s == null) {
            this.to = new HashSet<>();
        } else {
            this.to = new HashSet<>(s);
        }
        return this;
    }

    public NMail setCc(Set<String> s) {
        if (s == null) {
            this.cc = new HashSet<>();
        } else {
            this.cc = new HashSet<>(s);
        }
        return this;
    }

    public NMail setBcc(Set<String> s) {
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

    public List<NMailRecipient> getRecipients() {
        return recipients();
    }

    public List<NMailRecipient> recipients() {
        List<NMailRecipient> all = new ArrayList<>();
        for (String s : to) {
            all.add(new NMailRecipient(NMailRecipientType.TO, s));
        }
        for (String s : cto) {
            all.add(new NMailRecipient(NMailRecipientType.TOEACH, s));
        }
        for (String s : cc) {
            all.add(new NMailRecipient(NMailRecipientType.CC, s));
        }
        for (String s : bcc) {
            all.add(new NMailRecipient(NMailRecipientType.BCC, s));
        }
        return all;
    }

    public NMail addRecipients(Collection<NMailRecipient> recipients) {
        for (NMailRecipient recipient : recipients) {
            addRecipient(recipient);
        }
        return this;
    }

    public NMail addRecipient(NMailRecipient recipient) {
        return addRecipient(recipient.getType(), recipient.getValue());
    }

    public NMail addRecipient(NMailRecipientType type, String recipient) {
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

    public NMail addRecipients(NMailRecipientType recipientType, String... others) {
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

    public NMail setRecipients(NMailRecipientType recipientType, String... others) {
        return setRecipients(recipientType, Arrays.asList(others));
    }

    public NMail setRecipients(NMailRecipientType recipientType, Collection<String> others) {
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

    public Set<String> getRecipients(NMailRecipientType recipientType) {
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

    public NMail to(String... others) {
        to.addAll(Arrays.asList(others));
        return this;
    }

    public NMail cc(String... others) {
        cc.addAll(Arrays.asList(others));
        return this;
    }

    public NMail bcc(String... others) {
        bcc.addAll(Arrays.asList(others));
        return this;
    }

    public NMail to(Collection<String> others) {
        to.addAll(others);
        return this;
    }

    public NMail cc(Collection<String> others) {
        cc.addAll(others);
        return this;
    }

    public NMail bcc(Collection<String> others) {
        bcc.addAll(others);
        return this;
    }

    public NMail setProperty(String name, String value) {
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

    public NMail toeach(Collection<String> others) {
        cto.addAll(others);
        return this;
    }

    public NMail setToEach(Set<String> s) {
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

    public NMail toeach(String... others) {
        cto.addAll(Arrays.asList(others));
        return this;
    }

    public Map<String, NMailDataSource> namedDataSources() {
        return namedDataSources;
    }

    public NMailDataSource repeatDataSource() {
        return repeatDataSource;
    }

    public NMail repeatDatasource(NMailDataSource dataSource) {
        this.repeatDataSource = dataSource;
        return this;
    }

    public NMail setCredentials(String user, String password) {
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
        return new NMailModuleSerializer().toString(this);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.dry ? 1 : 0);
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
        final NMail other = (NMail) obj;
        if (this.dry != other.dry) {
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

    public void send(NMailListener listener) {
        DefaultNMailFactory.INSTANCE.createProcessor().sendMessage(this, null, listener);
    }

    public String getCwd() {
        return cwd;
    }

    public NMail setCwd(String cwd) {
        this.cwd = cwd;
        return this;
    }
}
