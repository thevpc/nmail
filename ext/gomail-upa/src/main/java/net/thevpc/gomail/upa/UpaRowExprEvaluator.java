///*
// * To change this license header, choose License Headers in Project Properties.
// *
// * and open the template in the editor.
// */
//package net.thevpc.gomail.upa;
//
//import net.thevpc.gomail.GoMailContext;
//import net.thevpc.gomail.GoMailDataSource;
//import net.thevpc.gomail.GoMailDataSourceRow;
//import net.thevpc.gomail.util.RowExprEvaluator;
//import net.thevpc.upa.QLTypeEvaluator;
//import net.thevpc.upa.QLEvaluator;
//import net.thevpc.upa.UPA;
//import net.thevpc.upa.expressions.Expression;
//import net.thevpc.upa.expressions.Literal;
//import net.thevpc.upa.expressions.Select;
//import net.thevpc.upa.expressions.Var;
//
///**
// *
// * @author taha.bensalah@gmail.com
// */
//public class UpaRowExprEvaluator implements RowExprEvaluator {
//
//    private GoMailDataSourceRow sourceRow;
//    private GoMailContext gomailContext;
//    private QLEvaluator ee;
//
//    public UpaRowExprEvaluator(GoMailContext context, final GoMailDataSourceRow row) {
//        this.sourceRow = row;
//        this.gomailContext = context;
//        ee = UPA.getBootstrap().getFactory().createObject(QLEvaluator.class);
//        ee.getRegistry().registerTypeEvaluator(Select.class, new QLTypeEvaluator() {
//
//            @Override
//            public Expression evalObject(Expression e, QLEvaluator evaluator, Object context) {
//                return evalObjectSelect((Select) e, evaluator, context);
//            }
//        });
//        ee.getRegistry().registerTypeEvaluator(Var.class, new QLTypeEvaluator() {
//
//            @Override
//            public Expression evalObject(Expression e, QLEvaluator evaluator, Object context) {
//                return evalObjectVar((Var) e, evaluator, context);
//            }
//        });
//    }
//
//    public String evalExpressionString(String expression) {
//        return expressionToString(evalString(expression), "");
//    }
//
//    public Expression evalString(String expression) {
//        return ee.evalString(expression, null);
//    }
//
//    public Expression evalObjectVar(Var v, QLEvaluator evaluator, Object context) {
//        if (v.getApplier() != null) {
//            throw new IllegalArgumentException("Unsupported");
//        }
//        String value = sourceRow.get(v.getName());
//        if (value == null) {
//            value = gomailContext.getProperties().getProperty(v.getName());
//        }
//        return new Literal(value);
//    }
//
//    public Expression evalObjectSelect(Select v, QLEvaluator evaluator, Object context) {
//        int fieldsCount = v.countFields();
//        StringBuilder all = new StringBuilder();
//        GoMailDataSource ds = gomailContext.getRegisteredDataSources().get(v.getEntityName());
//        if (ds == null) {
//            throw new IllegalArgumentException("Datasource " + v.getEntity() + " not found");
//        }
//        int rows = ds.getRowCount();
//        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
//            final GoMailDataSourceRow dsRowObject = ds.getRow(rowIndex);
//            QLEvaluator r = UPA.getBootstrap().getFactory().createObject(QLEvaluator.class);;
//            r.getRegistry().registerTypeEvaluator(Var.class, new QLTypeEvaluator() {
//
//                @Override
//                public Expression evalObject(Expression e, QLEvaluator evaluator, Object context) {
//                    Var v = (Var) e;
//                    if (v.getApplier() != null) {
//                        throw new IllegalArgumentException("Unsupported");
//                    }
//                    String value = dsRowObject.get(v.getName());
//                    if (value == null) {
//                        value = sourceRow.get(v.getName());
//                        if (value == null) {
//                            value = UpaRowExprEvaluator.this.gomailContext.getProperties().getProperty(v.getName());
//                        }
//                    }
//                    return new Literal(value);
//                }
//            });
//            boolean ok = false;
//            if (v.getWhere() == null) {
//                ok = true;
//            } else {
//                ok = expressionToBoolean(r.evalString(v.getWhere().toString(), context), ok);
//            }
//            if (ok) {
//                StringBuilder b = new StringBuilder();
//                for (int i = 0; i < fieldsCount; i++) {
//                    String s = expressionToString(r.evalString(v.getField(i).getExpression().toString(), context), "");
//                    if (s.length() > 0) {
//                        if (b.length() > 0) {
//                            b.append(",");
//                        }
//                        b.append(s);
//                    }
//                }
//                if (all.length() > 0) {
//                    all.append(",");
//                }
//                all.append(b);
//            }
//        }
//        return new Literal(all.toString());
//    }
//
//    public static boolean expressionToBoolean(String expression, boolean defaultValue) {
//        if(expression!=null && expression.length()>0){
//            return Boolean.parseBoolean(expression);
//        }
//        return defaultValue;
//    }
//
//    public static boolean expressionToBoolean(Expression expression, boolean defaultValue) {
//        boolean ok = defaultValue;
//        if (expression instanceof Literal) {
//            Object o = ((Literal) expression).getValue();
//            if (o instanceof Boolean) {
//                ok = ((Boolean) o).booleanValue();
//            } else if (o instanceof String) {
//                ok = Boolean.parseBoolean(o.toString());
//            } else if (o instanceof Number) {
//                ok = ((Number) o).doubleValue() != 0;
//            }
//        }
//        return ok;
//    }
//
//    public static String expressionToString(Expression expression, String defaultValue) {
//        if (expression instanceof Literal) {
//            Object o = ((Literal) expression).getValue();
//            if (o == null) {
//                return "";
//            } else {
//                return o.toString();
//            }
//        }
//        return defaultValue;
//    }
//}
