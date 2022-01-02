package net.thevpc.gomail.util;

import net.thevpc.gomail.GoMailDataSourceRow;
import net.thevpc.gomail.expr.Expr;
import net.thevpc.gomail.expr.ExprEvaluator;
import net.thevpc.gomail.expr.ExprParser;

import java.util.HashMap;
import java.util.Map;

public class RowEvaluator {
    GoMailDataSourceRow sourceRow;
    Map<String, Object> rowVars = new HashMap<>();

    public RowEvaluator(GoMailDataSourceRow row) {
        this.sourceRow = row;
        String[] columns = sourceRow.getColumns();
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            rowVars.put(column, sourceRow.get(i));
        }
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i].toLowerCase();
            if (!rowVars.containsKey(column)) {
                rowVars.put(column, sourceRow.get(i));
            }
        }

        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            rowVars.put(column, row.get(i));
        }
    }

    public <T> T eval(String expr, Class<T> expected,Map<String, Object> vars) {
        return eval(new ExprParser(expr).parseStatementList(),expected,vars);
    }

    public <T> T eval(Expr expr, Class<T> expected,Map<String, Object> vars) {
        ExprEvaluator ev = new ExprEvaluator();
        Map<String, Object> rowVars2=new HashMap<>(vars);
        rowVars2.putAll(this.rowVars);
        return ev.evalExpr(expr, expected, rowVars2);
    }
}
