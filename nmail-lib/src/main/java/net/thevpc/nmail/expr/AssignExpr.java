package net.thevpc.nmail.expr;

public class AssignExpr extends AbstractExpr {

    private Expr key;
    private Expr value;

    public AssignExpr(Expr key, Expr value) {
        this.key = key;
        this.value = value;
    }

    public Expr getKey() {
        return key;
    }

    public Expr getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    @Override
    public String asString() {
        return toString();
    }

}
