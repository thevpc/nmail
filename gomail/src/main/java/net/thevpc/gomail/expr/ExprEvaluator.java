package net.thevpc.gomail.expr;

import net.thevpc.gomail.GoMailDataSource;
import net.thevpc.gomail.datasource.GoMailDataSourceFilter;
import net.thevpc.gomail.datasource.factories.ServiceGoMailDataSourceFactory;
import net.thevpc.gomail.util.MyGoMailDataSourceFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class ExprEvaluator {
    public <T> T castTo(Object any, Class<T> expected) {
        if (expected.equals(String.class)) {
            return any == null ? null : (T) String.valueOf(any);
        }
        if (expected.equals(Boolean.class)) {
            if (any == null) {
                return null;
            }
            if (any instanceof Boolean) {
                return (T) any;
            }
            return (T) Boolean.valueOf(String.valueOf(any));
        }
        if (expected.equals(boolean.class)) {
            if (any == null) {
                return (T) (Object) false;
            }
            if (any instanceof Boolean) {
                return (T) any;
            }
            return (T) Boolean.valueOf(String.valueOf(any));
        }
        return (T) any;
    }

    public <T> T evalExpr(Expr expr, Class<T> expected, Map<String, Object> vars) {
        if (expr instanceof FctExpr) {
            FctExpr f = (FctExpr) expr;
            String n = f.getName();
            Expr[] a = f.getArgs();
            switch (n.toLowerCase()) {
                case "lowercase": {
                    if (a.length != 1) {
                        throw new IllegalArgumentException("invalid arguments");
                    }
                    return (T) castTo(evalExpr(a[0], String.class, vars).toLowerCase(), expected);
                }
                case "uppercase": {
                    if (a.length != 1) {
                        throw new IllegalArgumentException("invalid arguments");
                    }
                    return (T) castTo(evalExpr(a[0], String.class, vars).toUpperCase(), expected);
                }
            }
            throw new IllegalArgumentException("unsupported function " + f.getName());
        } else if (expr instanceof AssignExpr) {
            AssignExpr a = (AssignExpr) expr;
            Expr kk = a.getKey();
            if (kk instanceof WordExpr) {
                Object vv = evalExpr(a.getValue(), Object.class, vars);
                vars.put(((WordExpr) kk).getName(), vv);
                return (T) castTo(vv, expected);
            } else {
                throw new IllegalArgumentException("No yet supported " + expr);
            }
        } else if (expr instanceof OpExpr) {
            OpExpr op = (OpExpr) expr;
            switch (op.getOp()) {
                case PIPE: {
                    Expr[] arguments = op.getArguments();
                    GoMailDataSource filter = ServiceGoMailDataSourceFactory.getInstance().create("filter", new Expr[]{
                            arguments[0]
                            , arguments[1]});
                    for (int i = 2; i < arguments.length; i++) {
                        filter = ServiceGoMailDataSourceFactory.getInstance().create("filter", new Expr[]{
                                filter.toExpr()
                                , arguments[i]});
                    }
                    return (T) castTo(filter, expected);
                }
                case EQ:
                case NE:
                case LT:
                case LTE:
                case GT:
                case GTE: {
                    Expr[] arguments = op.getArguments();
                    Object a = evalExpr(arguments[0], Object.class, vars);
                    Object b = evalExpr(arguments[1], Object.class, vars);
                    if (!checkCompare(a, b, op.getOp())) {
                        return (T) castTo(false, expected);
                    }
                    a = b;
                    for (int i = 2; i < arguments.length; i++) {
                        b = evalExpr(arguments[i], expected, vars);
                        if (!checkCompare(a, b, op.getOp())) {
                            return (T) castTo(false, expected);
                        }
                        a = b;
                    }
                    return castTo(true, expected);
                }
                case PLUS:
                case MINUS:
                case DIV:
                case MUL: {
                    Expr[] arguments = op.getArguments();
                    Object a = evalExpr(arguments[0], Object.class, vars);
                    for (int i = 1; i < arguments.length; i++) {
                        Object b = evalExpr(arguments[i], Object.class, vars);
                        a = arith(a, b, op.getOp());
                    }
                    return castTo(a, expected);
                }
                case AND: {
                    Expr[] arguments = op.getArguments();
                    Object a = evalExpr(arguments[0], Object.class, vars);
                    if (!asBoolean(a)) {
                        return (T) castTo(false, expected);
                    }
                    for (int i = 1; i < arguments.length; i++) {
                        a = evalExpr(arguments[i], Object.class, vars);
                        if (!asBoolean(a)) {
                            return (T) castTo(false, expected);
                        }
                    }
                    return castTo(true, expected);
                }
                case OR: {
                    Expr[] arguments = op.getArguments();
                    Object a = evalExpr(arguments[0], Object.class, vars);
                    if (asBoolean(a)) {
                        return castTo(true, expected);
                    }
                    for (int i = 1; i < arguments.length; i++) {
                        a = evalExpr(arguments[i], Object.class, vars);
                        if (asBoolean(a)) {
                            return castTo(true, expected);
                        }
                    }
                    return (T) castTo(false, expected);
                }
                case SEMI_COLON: {
                    Expr[] arguments = op.getArguments();
                    Object a = null;
                    for (int i = 0; i < arguments.length; i++) {
                        a = evalExpr(arguments[i], (i == arguments.length - 1) ? expected : null, vars);
                    }
                    return castTo(a, expected);
                }
            }
            throw new IllegalArgumentException("unsupported operator " + op.getOp());
        } else if (expr instanceof StringExpr) {
            StringExpr s = (StringExpr) expr;
            return castTo(s.getValue(), expected);
        } else if (expr instanceof NumberExpr) {
            NumberExpr s = (NumberExpr) expr;
            return castTo(s.getValue(), expected);
        } else if (expr instanceof WordExpr) {
            WordExpr s = (WordExpr) expr;
            return getVariable(s.name, expected, vars);
        } else if (expr instanceof IfExpr) {
            IfExpr s = (IfExpr) expr;
            for (IfExpr.Branch branch : s.getBranches()) {
                Boolean ok = evalExpr(branch.getCondition(), Boolean.class, vars);
                if (ok) {
                    return evalExpr(branch.getValue(), expected, vars);
                }
            }
            Expr elseValue = s.getElseValue();
            if (elseValue == null) {
                return castTo(null, expected);
            }
            return evalExpr(elseValue, expected, vars);
        } else {
            throw new IllegalArgumentException("unsupported expression type " + expr.getClass());
        }
    }

    private <T> T getVariable(String name, Class<T> expected, Map<String, Object> vars) {
        switch (name) {
            case "null":
                return null;
            case "true":
                return castTo(true, expected);
            case "false":
                return castTo(false, expected);
        }
        Object varVal = null;
        if (!vars.containsKey(name)) {
            Map<String, String> m = vars.keySet().stream().collect(Collectors.toMap(String::toLowerCase, x -> x, (s, s2) -> s2));
            String lc = name.toLowerCase();
            if (!m.containsKey(lc)) {
                switch (lc) {
                    case "null":
                        return null;
                    case "true":
                        return castTo(true, expected);
                    case "false":
                        return castTo(false, expected);
                }
                throw new IllegalArgumentException("unrecognized variable " + name + ". available vars are : " + new TreeSet<>(vars.keySet()));
            } else {
                varVal = vars.get(lc);
            }
        } else {
            varVal = vars.get(name);
        }
        return castTo(varVal, expected);
    }

    public boolean asBoolean(Object a) {
        return a instanceof Boolean ? (boolean) a : false;
    }

    public int compare(Object a, Object b) {
        double aa = ((Number) a).doubleValue();
        double bb = ((Number) b).doubleValue();
        return Double.compare(aa, bb);
    }

    public boolean checkCompare(Object a, Object b, TokenTType op) {
        switch (op) {
            case EQ:
                return Objects.equals(a, b);
            case NE:
                return !Objects.equals(a, b);
            case LT:
                return compare(a, b) < 0;
            case LTE:
                return compare(a, b) <= 0;
            case GT:
                return compare(a, b) > 0;
            case GTE:
                return compare(a, b) >= 0;
        }
        throw new IllegalArgumentException("unsupported boolean operator " + op);
    }

    public Object arith(Object a, Object b, TokenTType op) {
        switch (op) {
            case PLUS: {
                return ((Number) a).doubleValue() + ((Number) b).doubleValue();
            }
            case MINUS: {
                return ((Number) a).doubleValue() - ((Number) b).doubleValue();
            }
            case MUL: {
                return ((Number) a).doubleValue() * ((Number) b).doubleValue();
            }
            case DIV: {
                return ((Number) a).doubleValue() / ((Number) b).doubleValue();
            }
        }
        throw new IllegalArgumentException("unsupported arithmetic operator " + op);
    }

}
