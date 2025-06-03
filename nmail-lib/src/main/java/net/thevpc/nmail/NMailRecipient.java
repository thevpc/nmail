package net.thevpc.nmail;

/**
 * @author taha.bensalah@gmail.com on 7/7/16.
 */
public class NMailRecipient {
    private NMailRecipientType type;
    private String value;

    public NMailRecipient(NMailRecipientType type, String value) {
        this.type = type;
        this.value = value;
    }

    public NMailRecipientType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
