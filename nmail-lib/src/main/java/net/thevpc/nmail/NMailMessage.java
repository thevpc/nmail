/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail;

import net.thevpc.nmail.modules.NMailModuleSerializer;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import net.thevpc.nmail.util.NMailUtils;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class NMailMessage implements Serializable, Cloneable {
    private String sourceId;
    private boolean dry = false;
    private String subject;
    private String from;
    private NMailBodyList body = new NMailBodyList();
    private Properties properties = new Properties();
    private Set<String> to = new HashSet<>();
    private Set<String> cc = new HashSet<>();
    private Set<String> bcc = new HashSet<>();

    public NMailMessage(NMail o) {
        properties = new Properties(o.getProperties());
        dry = o.isDry();
        subject = o.subject();
        from = o.from();
        body = body.copy();
        to = new HashSet(o.to());
        to.addAll(o.toeach());
        cc = new HashSet(o.cc());
        bcc = new HashSet(o.bcc());
    }


    public NMailMessage copy() {
        try {
            NMailMessage o = (NMailMessage) clone();
            o.properties = new Properties(properties);
            o.body = body.copy();
            o.to = new HashSet(to);
            o.cc = new HashSet(cc);
            o.bcc = new HashSet(bcc);
            return o;
        } catch (CloneNotSupportedException ex) {
            throw new IllegalArgumentException("Never");
        }
    }

    public NMailMessage setSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public String sourceId() {
        return sourceId;
    }

    public boolean isDry() {
        return dry;
    }

    public void setDry(boolean dry) {
        this.dry = dry;
    }

    public NMailMessage body(Object value, String type) {
        return body(value, type, NMailBodyPosition.OBJECT);
    }

    public NMailMessage header(Object value, String type) {
        return body(value, type, NMailBodyPosition.HEADER);
    }

    public NMailMessage attachment(Object value, String type) {
        return body(value, type, NMailBodyPosition.ATTACHMENT);
    }

    public NMailMessage footer(Object value, String type) {
        return body(value, type, NMailBodyPosition.FOOTER);
    }

    public NMailMessage body(Object value, String contentType, NMailBodyPosition position) {
        AbstractNMailBody theBody = null;
        if (value != null) {
            if (value instanceof String) {
                if (contentType == null) {
                    contentType = NMail.TEXT_CONTENT_TYPE;
                }
                theBody = new NMailBodyContent(((String) value).getBytes(), contentType, true);
            } else if (value instanceof CharSequence) {
                if (contentType == null) {
                    contentType = NMail.TEXT_CONTENT_TYPE;
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

    public void subject(String subject) {
        this.subject = subject;
    }

    public String from() {
        return from;
    }

    public void from(String from) {
        this.from = from;
    }

    public void setTo(Set<String> s) {
        if(s==null){
            this.to=new HashSet<>();
        }else{
            this.to=new HashSet<>(s);
        }
    }
    
    public void setCc(Set<String> s) {
        if(s==null){
            this.cc=new HashSet<>();
        }else{
            this.cc=new HashSet<>(s);
        }
    }
    
    public void setBcc(Set<String> s) {
        if(s==null){
            this.bcc=new HashSet<>();
        }else{
            this.bcc=new HashSet<>(s);
        }
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
        List<NMailRecipient> all=new ArrayList<>();
        for (String s : to) {
            all.add(new NMailRecipient(NMailRecipientType.TO, s));
        }
        for (String s : cc) {
            all.add(new NMailRecipient(NMailRecipientType.CC, s));
        }
        for (String s : bcc) {
            all.add(new NMailRecipient(NMailRecipientType.BCC, s));
        }
        return all;
    }

    public NMailMessage addRecipients(Collection<NMailRecipient> recipients) {
        for (NMailRecipient recipient : recipients) {
            addRecipient(recipient);
        }
        return this;
    }

    public NMailMessage addRecipient(NMailRecipient recipient) {
        return addRecipient(recipient.getType(),recipient.getValue());
    }

    public NMailMessage addRecipient(NMailRecipientType type, String recipient) {
        switch (type){
            case TO:{
                to(recipient);
                break;
            }
            case CC:{
                cc(recipient);
                break;
            }
            case BCC:{
                bcc(recipient);
                break;
            }
            default:{
                throw new IllegalArgumentException("Unsupported recipientType "+type);
            }
        }
        return this;
    }

    public NMailMessage addRecipients(NMailRecipientType recipientType, String... others) {
        switch (recipientType){
            case TO:{
                to(others);
                break;
            }
            case TOEACH:{
                to(others);
                break;
            }
            case CC:{
                cc(others);
                break;
            }
            case BCC:{
                bcc(others);
                break;
            }
            default:{
                throw new IllegalArgumentException("Unsupported recipientType "+recipientType);
            }
        }
        return this;
    }

    public NMailMessage setRecipients(NMailRecipientType recipientType, String... others) {
        return setRecipients(recipientType,Arrays.asList(others));
    }

    public NMailMessage setRecipients(NMailRecipientType recipientType, Collection<String> others) {
        switch (recipientType){
            case TO:{
                to().clear();
                to(others);
                break;
            }
            case CC:{
                cc().clear();
                cc(others);
                break;
            }
            case BCC:{
                bcc().clear();
                bcc(others);
                break;
            }
            case TOEACH:{
                throw new IllegalArgumentException("Unsupported recipientType "+recipientType);
            }
            default:{
                throw new IllegalArgumentException("Unsupported recipientType "+recipientType);
            }
        }
        return this;
    }

    public Set<String> getRecipients(NMailRecipientType recipientType) {
        switch (recipientType){
            case TO:{
                return to();
            }
            case CC:{
                return cc();
            }
            case BCC:{
                return bcc();
            }
            case TOEACH:{
                return Collections.EMPTY_SET;
            }
        }
        throw new IllegalArgumentException("Unsupported recipientType "+recipientType);
    }

    public NMailMessage to(String... others) {
        to.addAll(Arrays.asList(others));
        return this;
    }

    public NMailMessage cc(String... others) {
        cc.addAll(Arrays.asList(others));
        return this;
    }

    public NMailMessage bcc(String... others) {
        bcc.addAll(Arrays.asList(others));
        return this;
    }

    public NMailMessage to(Collection<String> others) {
        to.addAll(others);
        return this;
    }
    public NMailMessage cc(Collection<String> others) {
        cc.addAll(others);
        return this;
    }
    public NMailMessage bcc(Collection<String> others) {
        bcc.addAll(others);
        return this;
    }
    public void setProperty(String name, String value) {
        if (name != null) {
            if (value == null) {
                getProperties().remove(name);
            } else {
                getProperties().setProperty(name, value);
            }
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public NMail toNMail(){
        NMail m = new NMail();
        for (Map.Entry<Object, Object> entry : getProperties().entrySet()) {
            m.setProperty((String) entry.getKey(),(String) entry.getValue());
        }
        m.setDry(isDry());
        m.subject(subject());
        m.from(from());
        m.body().add(body());
        m.addRecipients(recipients());
        return m;
    }

    @Override
    public String toString() {
        return new NMailModuleSerializer().toString(this);
//        return XMailFormatter.format(this);
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
        final NMailMessage other = (NMailMessage) obj;
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
        return true;
    }
}
