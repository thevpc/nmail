package net.thevpc.gomail;

/**
 * @author taha.bensalah@gmail.com on 7/7/16.
 */
public class Recipient {
    private RecipientType type;
    private String value;

    public Recipient(RecipientType type, String value) {
        this.type = type;
        this.value = value;
    }

    public RecipientType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
