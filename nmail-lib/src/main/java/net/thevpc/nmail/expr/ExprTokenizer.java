package net.thevpc.nmail.expr;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.*;

public class ExprTokenizer {
    List<Token> pushedBack = new ArrayList<>();
    private StreamTokenizer st;
    private Map<String, TokenTType> special = new HashMap<>();

    public ExprTokenizer(String exprString) {
        this(new StreamTokenizer(new StringReader(exprString)));
        special.put("or", TokenTType.OR);
        special.put("and", TokenTType.AND);
    }

    public ExprTokenizer(StreamTokenizer st) {
        this.st = st;
    }

    public Token peek() {
        if (!pushedBack.isEmpty()) {
            return pushedBack.get(0);
        }
        Token n = nextToken();
        if (n != null) {
            pushBack(n);
        }
        return n;
    }

    public void pushBack(Token pushedBack) {
        if (pushedBack != null) {
            this.pushedBack.add(0, pushedBack);
        }
    }

    public Token nextToken() {
        if (!pushedBack.isEmpty()) {
            return pushedBack.remove(0);
        }
        return nextToken0();
    }

    public Token nextToken0() {
        int ttype = 0;
        try {
            ttype = st.nextToken();
            switch (ttype) {
                case StreamTokenizer.TT_EOF: {
                    return null;
                }
                case StreamTokenizer.TT_WORD: {
                    TokenTType a = special.get(st.sval.toLowerCase());
                    if (a != null) {
                        return new Token(a, st.sval, 0, 's');
                    }
                    return new Token(TokenTType.WORD, st.sval, 0, 'w');
                }
                case StreamTokenizer.TT_NUMBER: {
                    return new Token(TokenTType.NUMBER, String.valueOf(st.nval), st.nval, '0');
                }
                case '=': {
                    ttype = st.nextToken();
                    if (ttype == '=') {
                        return new Token(TokenTType.EQ, String.valueOf("=="), 0, '=');
                    } else if (ttype == StreamTokenizer.TT_EOF) {
                        return new Token(TokenTType.ASSIGN, String.valueOf("="), 0, '=');
                    } else {
                        st.pushBack();
                        return new Token(TokenTType.ASSIGN, String.valueOf("="), 0, '=');
                    }
                }
                case '!': {
                    ttype = st.nextToken();
                    if (ttype == '=') {
                        return new Token(TokenTType.NE, String.valueOf("!="), 0, '!');
                    } else if (ttype == StreamTokenizer.TT_EOF) {
                        return new Token(TokenTType.EXCLAM, String.valueOf("!"), 0, '!');
                    } else {
                        st.pushBack();
                        return new Token(TokenTType.EXCLAM, String.valueOf("!"), 0, '!');
                    }
                }
                case '<': {
                    ttype = st.nextToken();
                    switch (ttype) {
                        case '=': {
                            return new Token(TokenTType.LTE, String.valueOf("<="), 0, '<');
                        }
                        case '>': {
                            return new Token(TokenTType.NE, String.valueOf("<>"), 0, '<');
                        }
                        case StreamTokenizer.TT_EOF: {
                            return new Token(TokenTType.LT, String.valueOf("<"), 0, '<');
                        }
                        default: {
                            st.pushBack();
                            return new Token(TokenTType.LT, String.valueOf("<"), 0, '<');
                        }
                    }
                }
                case '>': {
                    ttype = st.nextToken();
                    switch (ttype) {
                        case '=': {
                            return new Token(TokenTType.GTE, String.valueOf(">="), 0, '>');
                        }
                        case StreamTokenizer.TT_EOF: {
                            return new Token(TokenTType.GT, String.valueOf(">"), 0, '>');
                        }
                        default: {
                            st.pushBack();
                            return new Token(TokenTType.GT, String.valueOf(">"), 0, '>');
                        }
                    }
                }
                case '|': {
                    ttype = st.nextToken();
                    switch (ttype) {
                        case '|': {
                            return new Token(TokenTType.OR, String.valueOf("||"), 0, '|');
                        }
                        case StreamTokenizer.TT_EOF: {
                            return new Token(TokenTType.PIPE, String.valueOf("|"), 0, '|');
                        }
                        default: {
                            st.pushBack();
                            return new Token(TokenTType.PIPE, String.valueOf("|"), 0, '|');
                        }
                    }
                }
                case '&': {
                    ttype = st.nextToken();
                    switch (ttype) {
                        case '&': {
                            return new Token(TokenTType.AND, String.valueOf("&&"), 0, '&');
                        }
                        case StreamTokenizer.TT_EOF: {
                            return new Token(TokenTType.AMPS, String.valueOf("&"), 0, '&');
                        }
                        default: {
                            st.pushBack();
                            return new Token(TokenTType.AMPS, String.valueOf("&"), 0, '&');
                        }
                    }
                }
                case '(': {
                    return new Token(TokenTType.PAR_OPEN, String.valueOf("("), 0, '(');
                }
                case ')': {
                    return new Token(TokenTType.PAR_CLOSE, String.valueOf("("), 0, ')');
                }
                case ';': {
                    return new Token(TokenTType.SEMI_COLON, String.valueOf(";"), 0, ';');
                }
                case ':': {
                    return new Token(TokenTType.COLON, String.valueOf(":"), 0, ':');
                }
                case ',': {
                    return new Token(TokenTType.COMMA, String.valueOf(","), 0, ',');
                }
                case '+': {
                    return new Token(TokenTType.PLUS, String.valueOf("+"), 0, '+');
                }
                case '-': {
                    return new Token(TokenTType.MINUS, String.valueOf("+"), 0, '-');
                }
                case '*': {
                    return new Token(TokenTType.MUL, String.valueOf("*"), 0, '*');
                }
                case '/': {
                    return new Token(TokenTType.DIV, String.valueOf("/"), 0, '/');
                }
                case '\'': {
                    return new Token(TokenTType.SQ_STRING, st.sval, 0, '\'');
                }
                case '\"': {
                    return new Token(TokenTType.DQ_STRING, st.sval, 0, '\"');
                }
                default: {
                    return new Token(TokenTType.CHAR, String.valueOf((char) ttype), 0, (char) ttype);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
