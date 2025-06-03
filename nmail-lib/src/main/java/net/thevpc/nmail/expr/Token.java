package net.thevpc.nmail.expr;

public class Token {
    TokenTType ttype;
    String sval;
    double nval;
    char cval;

    public Token(TokenTType ttype, String sval, double nval, char cval) {
        this.ttype = ttype;
        this.sval = sval;
        this.nval = nval;
    }

    public TokenTType getTtype() {
        return ttype;
    }

    public String getSval() {
        return sval;
    }

    public double getNval() {
        return nval;
    }

    public char getCval() {
        return cval;
    }

    @Override
    public String toString() {
        return "Token{" +
                "ttype=" + ttype +
                ", sval='" + sval + '\'' +
                ", nval=" + nval +
                ", cval=" + cval +
                '}';
    }
}
