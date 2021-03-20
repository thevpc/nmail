/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.gomail.util;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author vpc
 */
public class ExprList {

    private List<Expr> values = new ArrayList<>();
    private StreamTokenizer st;

    public ExprList() {

    }

    public ExprList(String propValue) {
        st = new StreamTokenizer(new StringReader(propValue));
        int ttype;
        while (true) {
            try {
                ttype = st.nextToken();
                if (ttype == StreamTokenizer.TT_EOF) {
                    break;
                } else if (ttype == ';') {
                    //ignore
                } else {
                    st.pushBack();
                    Expr e = parseExpr();
                    if (e != null) {
                        values.add(e);
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    public ExprList add(int index, Expr a) {
        values.add(index, a);
        return this;
    }

    public ExprList add(Expr a) {
        values.add(a);
        return this;
    }

    public ExprList addAll(List<Expr> a) {
        values.addAll(a);
        return this;
    }

    public ExprList addAll(Expr... a) {
        values.addAll(Arrays.asList(a));
        return this;
    }

    public Expr get(int i) {
        return values.get(i);
    }

    public int size() {
        return values.size();
    }

    public Expr[] toArray() {
        return toList().toArray(new Expr[0]);
    }

    public Expr getValue(String key) {
        for (Expr value : values) {
            KeyValExpr kv = value.toKeyValExpr();
            if (kv != null && kv.getKey().toWordExpr() != null && kv.getKey().toWordExpr().getValue().equals(key)) {
                return kv.getValue();
            }
        }
        return null;
    }

    public List<Expr> toList() {
        return new ArrayList<>(values);
    }

    private Expr parseExpr() throws IOException {
        int ttype = st.nextToken();
        if (ttype == StreamTokenizer.TT_EOF) {
            throw new RuntimeException("Encountred EOF");
        } else if (ttype == ';' || ttype == ',') {
            //ignore
            st.pushBack();
            return null;
        } else {
            st.pushBack();
            Expr e = parseExprSimple();
            if (e instanceof WordExpr) {
                ttype = st.nextToken();
                if (ttype == '=') {
                    Expr v = parseExprSimple();
                    e = new KeyValExpr(e, v);
                } else {
                    st.pushBack();
                }
            }
            return e;
        }
    }

    private List<Expr> parsePars() throws IOException {
        int ttype = st.nextToken();
        if (ttype == '(') {
            ttype = st.nextToken();
            List<Expr> a = new ArrayList<>();
            if (ttype == ')') {
                return a;
            } else if (ttype == ',') {
                a.add(null);
            } else {
                st.pushBack();
                Expr e = parseExpr();
                if (e == null) {
                    throw new IllegalArgumentException("Missing expression");
                }
                a.add(e);
            }
            while (true) {
                ttype = st.nextToken();
                if (ttype == StreamTokenizer.TT_EOF) {
                    throw new IllegalArgumentException("Missing ')'");
                }
                if (ttype == ')') {
                    return a;
                } else if (ttype == ',') {
                    ttype = st.nextToken();
                    if (ttype == ',' || ttype == ')' || ttype == StreamTokenizer.TT_EOF) {
                        a.add(null);
                    } else {
                        st.pushBack();
                        Expr e = parseExpr();
                        if (e == null) {
                            throw new IllegalArgumentException("Missing expression");
                        }
                        a.add(e);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Missing '('");
        }
    }

    private Expr parseExprSimple() throws IOException {
        int ttype = st.nextToken();
        if (ttype == StreamTokenizer.TT_EOF) {
            throw new RuntimeException("Encountred EOF");
        } else if (ttype == ';') {
            return null;
        } else {
            if (ttype == StreamTokenizer.TT_WORD) {
                String n = st.sval;
                ttype = st.nextToken();
                if (ttype == '(') {
                    st.pushBack();
                    List<Expr> r = parsePars();
                    return new FctExpr(n, r.toArray(new Expr[0]));
                }
                if (ttype != StreamTokenizer.TT_EOF) {
                    st.pushBack();
                }
                return new WordExpr(n);
            } else if (ttype == '"') {
                String n = st.sval;
                return new StringExpr(n);
            } else if (ttype == '\'') {
                String n = st.sval;
                return new StringExpr(n);
            } else if (ttype == StreamTokenizer.TT_NUMBER) {
                double n = st.nval;
                return new NumberExpr(n);
            } else {
                throw new IllegalArgumentException("Unexpected " + (char) +ttype);
            }
        }
    }

    public static KeyValExpr createKeyValue(String key, String value) {
        return new ExprList.KeyValExpr(new ExprList.WordExpr(key), new ExprList.StringExpr(value));
    }

    public static boolean isWord(Expr e) {
        return e instanceof WordExpr;
    }

    public static boolean isString(Expr e) {
        return e instanceof StringExpr;
    }

    public static String toWord(Expr e) {
        if (e instanceof WordExpr) {
            return ((WordExpr) e).value;
        }
        return null;
    }

    public Expr searchValueByKey(String key) {
        for (Expr expr : values) {
            if (key.equals(toWordKey(expr))) {
                return ((KeyValExpr) expr).getValue();
            }
        }
        return null;
    }

    public static Expr searchValueByKey(Expr[] all, String key) {
        for (Expr expr : all) {
            if (key.equals(toWordKey(expr))) {
                return ((KeyValExpr) expr).getValue();
            }
        }
        return null;
    }

    public static String toString(Expr e) {
        if (e instanceof StringExpr) {
            return ((StringExpr) e).value;
        }
        return null;
    }

    public static String toWordKey(Expr e) {
        if (e instanceof KeyValExpr) {
            Expr k = ((KeyValExpr) e).key;
            return toWord(k);
        }
        return null;
    }

    public static String toStringValue(Expr e) {
        if (e instanceof KeyValExpr) {
            Expr k = ((KeyValExpr) e).value;
            if (k instanceof StringExpr) {
                return ((StringExpr) k).value;
            }
        }
        return null;
    }

    public static interface Expr {

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

        KeyValExpr toKeyValExpr();

        FctExpr toFctExpr();
    }

    public static abstract class AbstractExpr implements Expr {

        @Override
        public boolean isWord() {
            return toWordExpr() != null;
        }

        @Override
        public boolean isString() {
            return toStringExpr() != null;
        }

        @Override
        public boolean isNumber() {
            return toNumberExpr() != null;
        }

        @Override
        public boolean isKeyVal() {
            return toKeyValExpr() != null;
        }

        @Override
        public boolean isFct() {
            return toFctExpr() != null;
        }

        @Override
        public boolean isWord(String n) {
            WordExpr w = toWordExpr();
            if (w != null && w.getValue().equals(n)) {
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
        public StringExpr toStringExpr() {
            if (this instanceof StringExpr) {
                return (StringExpr) this;
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
        public KeyValExpr toKeyValExpr() {
            if (this instanceof KeyValExpr) {
                return (KeyValExpr) this;
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

    public static class StringExpr extends AbstractExpr {

        private String value;

        public StringExpr(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String asString() {
            return value;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\"");
            for (char c : value.toCharArray()) {
                switch (c) {
                    case '\n': {
                        sb.append("\\n");
                        break;
                    }
                    case '\r': {
                        sb.append("\\r");
                        break;
                    }
                    case '\t': {
                        sb.append("\\t");
                        break;
                    }
                    case '\f': {
                        sb.append("\\f");
                        break;
                    }
                    default: {
                        sb.append(c);
                    }
                }
            }
            sb.append("\"");
            return sb.toString();
        }

    }

    public static class NumberExpr extends AbstractExpr {

        private double value;

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

    public static class WordExpr extends AbstractExpr {

        private String value;

        public WordExpr(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String asString() {
            return value;
        }

    }

    public static class FctExpr extends AbstractExpr {

        private String name;
        private Expr[] args;

        public FctExpr(String name, Expr[] args) {
            this.name = name;
            this.args = args;
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

    public static class KeyValExpr extends AbstractExpr {

        private Expr key;
        private Expr value;

        public KeyValExpr(Expr key, Expr value) {
            this.key = key;
            this.value = value;
        }

        public Expr getKey() {
            return key;
        }

        public Expr getValue() {
            return value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

        @Override
        public String asString() {
            return toString();
        }

    }
}
