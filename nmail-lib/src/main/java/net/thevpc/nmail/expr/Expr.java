package net.thevpc.nmail.expr;

public interface Expr {

    String asString();

    boolean isWord(String n);

    boolean isString(String n);

    boolean isWordOrString(String n);

    boolean isWord();

    boolean isString();

    boolean isOp();
    boolean isOp(TokenTType t);
    boolean isAssign();

    boolean isFunction();

    boolean isNumber();

    boolean isKeyVal();

    boolean isFct();

    AssignExpr toAssign();

    FctExpr toFunction();
    OpExpr toOp();

    StringExpr toStringExpr();

    WordExpr toWordExpr();

    NumberExpr toNumberExpr();

    AssignExpr toAssignExpr();

    FctExpr toFctExpr();
}
