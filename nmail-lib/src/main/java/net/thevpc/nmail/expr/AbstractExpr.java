package net.thevpc.nmail.expr;

public abstract class AbstractExpr implements Expr {

    @Override
    public boolean isWord(String n) {
        WordExpr w = toWordExpr();
        if (w != null && w.getName().equals(n)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isString(String n) {
        StringExpr w = toStringExpr();
        if (w != null && w.getValue().equals(n)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isWordOrString(String n) {
        return isWord(n) || isString(n);
    }

    @Override
    public boolean isWord() {
        return toWordExpr() != null;
    }

    @Override
    public boolean isString() {
        return toStringExpr() != null;
    }

    @Override
    public boolean isOp() {
        return this instanceof OpExpr;
    }

    @Override
    public boolean isOp(TokenTType t) {
        return isOp() && ((OpExpr) this).getOp() == t;
    }

    @Override
    public boolean isAssign() {
        return this instanceof AssignExpr;
    }

    @Override
    public boolean isFunction() {
        return this instanceof FctExpr;
    }

    @Override
    public FctExpr toFunction() {
        if (this instanceof FctExpr) {
            return (FctExpr) this;
        }
        return null;
    }

    @Override
    public OpExpr toOp() {
        if (this instanceof OpExpr) {
            return (OpExpr) this;
        }
        return null;
    }

    @Override
    public boolean isNumber() {
        return toNumberExpr() != null;
    }

    @Override
    public boolean isKeyVal() {
        return toAssignExpr() != null;
    }

    @Override
    public boolean isFct() {
        return toFctExpr() != null;
    }

    @Override
    public StringExpr toStringExpr() {
        if (this instanceof StringExpr) {
            return (StringExpr) this;
        }
        return null;
    }
    @Override
    public AssignExpr toAssign() {
        if (this instanceof AssignExpr) {
            return (AssignExpr) this;
        }
        return null;
    }

    @Override
    public WordExpr toWordExpr() {
        if (this instanceof WordExpr) {
            return (WordExpr) this;
        }
        return null;
    }

    @Override
    public NumberExpr toNumberExpr() {
        if (this instanceof NumberExpr) {
            return (NumberExpr) this;
        }
        return null;
    }

    @Override
    public AssignExpr toAssignExpr() {
        if (this instanceof AssignExpr) {
            return (AssignExpr) this;
        }
        return null;
    }

    @Override
    public FctExpr toFctExpr() {
        if (this instanceof FctExpr) {
            return (FctExpr) this;
        }
        return null;
    }

}
