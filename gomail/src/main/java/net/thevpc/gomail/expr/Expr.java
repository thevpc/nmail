package net.thevpc.gomail.expr;

public interface Expr {

    String asString();

    boolean isWord(String n);

    boolean isString(String n);

    boolean isWordOrString(String n);

    boolean isWord();

    boolean isString();

    boolean isNumber();

    boolean isKeyVal();

    boolean isFct();

    StringExpr toStringExpr();

    WordExpr toWordExpr();

    NumberExpr toNumberExpr();

    AssignExpr toAssignExpr();

    FctExpr toFctExpr();
}
