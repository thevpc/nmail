package net.thevpc.nmail.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ExprParser {
    private ExprTokenizer st;

    public ExprParser(String exprString) {
        this(new ExprTokenizer(exprString));
    }

    public ExprParser(ExprTokenizer tok) {
        this.st = tok;
    }

    private List<Expr> parsePars() {
        Token ttype = st.nextToken();
        if (ttype != null && ttype.ttype == TokenTType.PAR_OPEN) {
            ttype = st.nextToken();
            List<Expr> a = new ArrayList<>();
            if (ttype != null && ttype.ttype == TokenTType.PAR_CLOSE) {
                return a;
            } else if (ttype != null && ttype.ttype == TokenTType.COMMA) {
                a.add(null);
            } else {
                st.pushBack(ttype);
                Expr e = parseExpr();
                if (e == null) {
                    throw new IllegalArgumentException("Missing expression");
                }
                a.add(e);
            }
            while (true) {
                ttype = st.nextToken();
                if (ttype == null) {
                    throw new IllegalArgumentException("Missing ')'");
                }
                if (ttype.ttype == TokenTType.PAR_CLOSE) {
                    return a;
                } else if (ttype.ttype == TokenTType.COMMA) {
                    ttype = st.nextToken();
                    if (ttype == null || ttype.ttype == TokenTType.COMMA || ttype.ttype == TokenTType.PAR_CLOSE) {
                        a.add(null);
                    } else {
                        st.pushBack(ttype);
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


    public Expr parseStatementList() {
        Expr e = parseOp(new TokenTType[]{
                TokenTType.SEMI_COLON
        }, this::parseStatement);
        if (st.peek() != null) {
            throw new IllegalArgumentException("unable to process " + st.peek());
        }
        return e;
    }


    private Expr parseExpr() {
        return parseExprPipe();
    }

    private Expr parseStatement() {
        Token a = st.nextToken();
        if (a == null) {
            return null;
        }
        if (a.ttype == TokenTType.WORD) {
            Token b = st.nextToken();
            if (b == null) {
                return new WordExpr(a.sval);
            } else if (b.ttype == TokenTType.ASSIGN) {
                Expr e = parseExpr();
                Token afterB = st.peek();
                if (afterB != null) {
                    throw new IllegalArgumentException("expected expression");
                }
                if (e == null) {
                    throw new IllegalArgumentException("expected unable to parse expression starting with " + afterB);
                }
                return new AssignExpr(new WordExpr(a.sval), e);
            } else {
                st.pushBack(b);
                st.pushBack(a);
                return parseExpr();
            }
        } else {
            st.pushBack(a);
            return parseExpr();
        }
    }


    private Expr parseExprPipe() {
        return parseOp(new TokenTType[]{TokenTType.PIPE}, this::parseExprOr);
    }

    private Expr parseExprOr() {
        return parseOp(new TokenTType[]{TokenTType.OR}, this::parseExprAnd);
    }

    private Expr parseExprAnd() {
        return parseOp(new TokenTType[]{TokenTType.AND}, this::parseExprCompare);
    }

    private Expr parseExprCompare() {
        return parseOp(new TokenTType[]{
                TokenTType.LT, TokenTType.LTE, TokenTType.GT, TokenTType.GTE, TokenTType.EQ, TokenTType.NE
        }, this::parseExprPlus);
    }

    private Expr parseExprPlus() {
        return parseOp(new TokenTType[]{
                TokenTType.PLUS, TokenTType.MINUS
        }, this::parseExprMul);
    }

    private Expr parseExprMul() {
        return parseOp(new TokenTType[]{
                TokenTType.MUL, TokenTType.DIV
        }, this::parseTerminal);
    }

    private Expr parseOp(TokenTType[] types, Supplier<Expr> next) {
        List<Expr> all = new ArrayList<>();
        Expr e = next.get();
        if (e == null) {
            return null;
        }
        all.add(e);
        TokenTType old = null;
        while (true) {
            Token a = st.peek();
            if (a != null) {
                TokenTType nt = null;
                for (TokenTType type : types) {
                    if (a.ttype == type) {
                        nt = type;
                        break;
                    }
                }
                if (nt != null) {
                    st.nextToken(); // consume operator
                    Expr e2 = next.get();
                    if (e2 == null) {
                        throw new IllegalArgumentException("expected expression after " + nt);
                    }
                    if (old != nt && all.size() > 1) {
                        List<Expr> all2 = new ArrayList<>(all);
                        all.clear();
                        all.add(new OpExpr(old, all2.toArray(new Expr[0])));
                    }
                    all.add(e2);
                    old = nt;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if (all.isEmpty()) {
            return null;
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new OpExpr(old, all.toArray(new Expr[0]));
    }


    private Expr parseIf() {
        Token token = st.nextToken();
        if (token == null) {
            return null;
        }


        List<IfExpr.Branch> conds = new ArrayList<>();
        Expr elseValue = null;
        if (token.ttype == TokenTType.WORD && token.sval.equals("if")) {
            Expr cond0Cond = parseExpr();
            if (cond0Cond == null) {
                throw new IllegalArgumentException("expected condition buts was "+st.peek());
            }
            Token then = st.peek();
            if (then != null && then.ttype == TokenTType.WORD && then.sval.equals("then")) {
                st.nextToken();
                Expr cond0Value = parseExpr();
                if (cond0Value == null) {
                    throw new IllegalArgumentException("expected trueValue but was "+st.peek());
                }
                conds.add(new IfExpr.Branch(cond0Cond, cond0Value));
                while (true) {
                    Token elseOrEnd = st.peek();
                    if (elseOrEnd != null && elseOrEnd.ttype == TokenTType.WORD && elseOrEnd.sval.equals("else")) {
                        st.nextToken();
                        Token nextIf = st.peek();
                        if (nextIf != null && nextIf.ttype == TokenTType.WORD && nextIf.sval.equals("if")) {
                            Expr cond1Cond = parseExpr();
                            if (cond1Cond == null) {
                                throw new IllegalArgumentException("expected condition but was "+st.peek());
                            }
                            Token then1 = st.peek();
                            if (then1 != null && then1.ttype == TokenTType.WORD && then1.sval.equals("then")) {
                                st.nextToken();
                                Expr cond1Value = parseExpr();
                                if (cond1Value == null) {
                                    throw new IllegalArgumentException("expected trueValue");
                                }
                                conds.add(new IfExpr.Branch(cond1Cond, cond1Value));
                            } else {
                                throw new IllegalArgumentException("expected 'then' but was "+then1);
                            }
                        } else {
                            elseValue = parseExpr();
                            if (elseValue == null) {
                                throw new IllegalArgumentException("expected 'else' expression buts was "+st.peek());
                            }
                            Token end = st.peek();
                            if (end != null && end.ttype == TokenTType.WORD && end.sval.equals("end")) {
                                st.nextToken();
                                //ok
                                break;
                            } else {
                                throw new IllegalArgumentException("expected 'end' but was "+end);
                            }
                        }

                    } else if (elseOrEnd != null && elseOrEnd.ttype == TokenTType.WORD && elseOrEnd.sval.equals("end")) {
                        st.nextToken();
                        break;
                    } else {
                        throw new IllegalArgumentException("expected 'else' or 'end' but was "+elseOrEnd);
                    }
                }
            } else {
                throw new IllegalArgumentException("expected 'then' but was "+then);
            }
            return new IfExpr(conds.toArray(new IfExpr.Branch[0]), elseValue);
        } else {
            st.pushBack(token);
            return null;
        }
    }

    private Expr parseTerminal() {
        Token token = st.nextToken();
        if (token == null) {
            return null;
        } else {
            switch (token.ttype) {
                case WORD: {
                    if (token.sval.equals("if")) {
                        st.pushBack(token);
                        return parseIf();
                    }
                    Token a = st.peek();
                    if (a != null) {
                        if (a.ttype == TokenTType.PAR_OPEN) {
                            List<Expr> args = parsePars();
                            return new FctExpr(
                                    token.sval, args.toArray(new Expr[0])
                            );
                        }
                    }
                    return new WordExpr(token.sval);
                }
                case DQ_STRING:
                case SQ_STRING: {
                    return new StringExpr(token.sval);
                }
                case NUMBER: {
                    return new NumberExpr(token.nval);
                }
                case PAR_OPEN: {
                    Expr r = parseExpr();
                    if (r == null) {
                        throw new IllegalArgumentException("expected expression after (");
                    }
                    Token t = st.nextToken();
                    if (t == null) {
                        throw new IllegalArgumentException("expected )");
                    }
                    if (t.ttype != TokenTType.PAR_CLOSE) {
                        throw new IllegalArgumentException("expected ) but got " + t);
                    }
                    return r;
                }
                default: {
                    return null;
                }
            }
        }
    }
}
