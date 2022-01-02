package net.thevpc.gomail.expr;

public class WordExpr extends AbstractExpr {

    String name;

    public WordExpr(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public String asString() {
        return name;
    }

}
