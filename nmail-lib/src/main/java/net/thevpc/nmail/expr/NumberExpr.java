package net.thevpc.nmail.expr;

public class NumberExpr extends AbstractExpr {

    private double value;

    public double getValue() {
        return value;
    }

    public NumberExpr(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public String asString() {
        return String.valueOf(value);
    }

}
