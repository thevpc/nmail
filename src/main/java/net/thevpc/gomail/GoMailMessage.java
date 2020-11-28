/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail;

import net.thevpc.gomail.modules.GoMailModuleSerializer;
import net.thevpc.common.io.FileUtils;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class GoMailMessage implements Serializable, Cloneable {
    private boolean simulate = false;
    private String subject;
    private String from;
    private GoMailBodyList body = new GoMailBodyList();
    private Properties properties = new Properties();
    private Set<String> to = new HashSet<>();
    private Set<String> cc = new HashSet<>();
    private Set<String> bcc = new HashSet<>();

    public GoMailMessage(GoMail o) {
        properties = new Properties(o.getProperties());
        simulate = o.isSimulate();
        subject = o.subject();
        from = o.from();
        body = body.copy();
        to = new HashSet(o.to());
        to.addAll(o.toeach());
        cc = new HashSet(o.cc());
        bcc = new HashSet(o.bcc());
    }


    public GoMailMessage copy() {
        try {
            GoMailMessage o = (GoMailMessage) clone();
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

    public boolean isSimulate() {
        return simulate;
    }

    public void setSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public GoMailMessage body(Object value, String type) {
        return body(value, type, GoMailBodyPosition.OBJECT);
    }

    public GoMailMessage header(Object value, String type) {
        return body(value, type, GoMailBodyPosition.HEADER);
    }

    public GoMailMessage attachment(Object value, String type) {
        return body(value, type, GoMailBodyPosition.ATTACHMENT);
    }

    public GoMailMessage footer(Object value, String type) {
        return body(value, type, GoMailBodyPosition.FOOTER);
    }

    public GoMailMessage body(Object value, String contentType, GoMailBodyPosition position) {
        AbstractGoMailBody theBody = null;
        if (value != null) {
            if (value instanceof String) {
                if (contentType == null) {
                    contentType = GoMail.TEXT_CONTENT_TYPE;
                }
                theBody = new GoMailBodyContent(((String) value).getBytes(), contentType, true);
            } else if (value instanceof CharSequence) {
                if (contentType == null) {
                    contentType = GoMail.TEXT_CONTENT_TYPE;
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

    public List<Recipient> getRecipients() {
        return recipients();
    }

    public List<Recipient> recipients() {
        List<Recipient> all=new ArrayList<>();
        for (String s : to) {
            all.add(new Recipient(RecipientType.TO, s));
        }
        for (String s : cc) {
            all.add(new Recipient(RecipientType.CC, s));
        }
        for (String s : bcc) {
            all.add(new Recipient(RecipientType.BCC, s));
        }
        return all;
    }

    public GoMailMessage addRecipients(Collection<Recipient> recipients) {
        for (Recipient recipient : recipients) {
            addRecipient(recipient);
        }
        return this;
    }

    public GoMailMessage addRecipient(Recipient recipient) {
        return addRecipient(recipient.getType(),recipient.getValue());
    }

    public GoMailMessage addRecipient(RecipientType type,String recipient) {
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

    public GoMailMessage addRecipients(RecipientType recipientType,String... others) {
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

    public GoMailMessage setRecipients(RecipientType recipientType,String... others) {
        return setRecipients(recipientType,Arrays.asList(others));
    }

    public GoMailMessage setRecipients(RecipientType recipientType,Collection<String> others) {
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

    public Set<String> getRecipients(RecipientType recipientType) {
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

    public GoMailMessage to(String... others) {
        to.addAll(Arrays.asList(others));
        return this;
    }

    public GoMailMessage cc(String... others) {
        cc.addAll(Arrays.asList(others));
        return this;
    }

    public GoMailMessage bcc(String... others) {
        bcc.addAll(Arrays.asList(others));
        return this;
    }

    public GoMailMessage to(Collection<String> others) {
        to.addAll(others);
        return this;
    }
    public GoMailMessage cc(Collection<String> others) {
        cc.addAll(others);
        return this;
    }
    public GoMailMessage bcc(Collection<String> others) {
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

    public GoMail toGoMail(){
        GoMail m = new GoMail();
        for (Map.Entry<Object, Object> entry : getProperties().entrySet()) {
            m.setProperty((String) entry.getKey(),(String) entry.getValue());
        }
        m.setSimulate(isSimulate());
        m.subject(subject());
        m.from(from());
        m.body().add(body());
        m.addRecipients(recipients());
        return m;
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
        final GoMailMessage other = (GoMailMessage) obj;
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
        return true;
    }
}
