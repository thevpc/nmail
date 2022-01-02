package net.thevpc.gomail.expr;

public class ExprHelper {
    public static String getOpString(TokenTType type) {
        switch (type){
            case PIPE:return "|";
            case ASSIGN:return "=";
            case EQ:return "==";
            case NE:return "!=";
            case DIV:return "/";
            case MUL:return "*";
            case EXCLAM:return "!";
            case LTE:return "<=";
            case GTE:return ">=";
            case LT:return "<";
            case GT:return ">";
            case COLON:return ":";
            case SEMI_COLON:return ";";
            case AMPS:return "&";
            case AND:return "and";
            case OR:return "or";
            case MINUS:return "-";
            case PLUS:return "+";
        }
        throw new IllegalArgumentException("not an operator");
    }
    public static Expr getValue(String key, Expr... statements) {
        for (Expr value : statements) {
            AssignExpr kv = value.toAssignExpr();
            if (kv != null && kv.getKey().toWordExpr() != null && kv.getKey().toWordExpr().getName().equals(key)) {
                return kv.getValue();
            }
        }
        return null;
    }

    public static Expr[] toStatements(Expr expr) {
        if(expr==null){
            return new Expr[0];
        }
        if(expr instanceof OpExpr){
            OpExpr o=(OpExpr) expr;
            if(o.getOp()==TokenTType.SEMI_COLON){
                return o.getArguments();
            }
        }
        return new Expr[]{expr};
    }

    public static Expr searchValueByKey(String key,Expr... statements) {
        for (Expr expr : statements) {
            if (key.equals(ExprHelper.toWordKey(expr))) {
                return ((AssignExpr) expr).getValue();
            }
        }
        return null;
    }


    public static Expr op(TokenTType op, Expr... all) {
        return new OpExpr(op, all);
    }

    public static AssignExpr assign(String key, String value) {
        return new AssignExpr(new WordExpr(key), new StringExpr(value));
    }

    public static boolean isWord(Expr e) {
        return e instanceof WordExpr;
    }

    public static boolean isString(Expr e) {
        return e instanceof StringExpr;
    }

    public static String toWord(Expr e) {
        if (e instanceof WordExpr) {
            return ((WordExpr) e).name;
        }
        return null;
    }

    public static Expr searchValueByKey(Expr[] all, String key) {
        for (Expr expr : all) {
            if (key.equals(toWordKey(expr))) {
                return ((AssignExpr) expr).getValue();
            }
        }
        return null;
    }

    public static String toString(Expr e) {
        if (e instanceof StringExpr) {
            return ((StringExpr) e).getValue();
        }
        return null;
    }

    public static String toWordKey(Expr e) {
        if (e instanceof AssignExpr) {
            Expr k = ((AssignExpr) e).getKey();
            return toWord(k);
        }
        return null;
    }

    public static String toStringValue(Expr e) {
        if (e instanceof AssignExpr) {
            Expr k = ((AssignExpr) e).getValue();
            if (k instanceof StringExpr) {
                return ((StringExpr) k).getValue();
            }
        }
        return null;
    }
}
