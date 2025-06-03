package net.thevpc.nmail.expr;

public class StringExpr extends AbstractExpr {

    private String value;

    public StringExpr(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\n': {
                    sb.append("\\n");
                    break;
                }
                case '\r': {
                    sb.append("\\r");
                    break;
                }
                case '\t': {
                    sb.append("\\t");
                    break;
                }
                case '\f': {
                    sb.append("\\f");
                    break;
                }
                case '\\': {
                    sb.append("\\\\");
                    break;
                }
                case '"': {
                    sb.append("\\\"");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

}
