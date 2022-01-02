package net.thevpc.gomail.expr;

public class OpExpr extends AbstractExpr {

    private TokenTType op;
    private Expr[] args;

    public OpExpr(TokenTType type, Expr... args) {
        this.op = type;
        this.args = args;
    }

    public TokenTType getOp() {
        return op;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                if (op == TokenTType.SEMI_COLON) {
                    sb.append(";\n");
                } else {
                    String s = ExprHelper.getOpString(op);
                    sb.append(" ");
                    sb.append(s);
                    sb.append(" ");
                }
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

    public Expr[] getArguments() {
        return args;
    }
}
