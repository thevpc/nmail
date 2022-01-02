package net.thevpc.gomail.expr;

public class FctExpr extends AbstractExpr {

    private String name;
    private Expr[] args;

    public FctExpr(String name, Expr[] args) {
        this.name = name;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public Expr[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name).append("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(args[i]);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String asString() {
        return toString();
    }

}
