package net.thevpc.nmail.util;

import net.thevpc.nmail.NMailDataSourceRow;
import net.thevpc.nmail.expr.Expr;
import net.thevpc.nmail.expr.ExprEvaluator;
import net.thevpc.nmail.expr.ExprParser;
import net.thevpc.nmail.expr.ExprVars;

import java.util.HashMap;
import java.util.Map;

public class RowEvaluator {
    NMailDataSourceRow sourceRow;
    Map<String, Object> rowVars = new HashMap<>();

    public RowEvaluator(NMailDataSourceRow row) {
        this.sourceRow = row;
        String[] columns = sourceRow.getColumns();
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            rowVars.put(column, sourceRow.get(i));
        }

        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            rowVars.put(column, row.get(i));
        }
    }

    public <T> T eval(String expr, Class<T> expected, ExprVars vars) {
        return eval(new ExprParser(expr).parseStatementList(),expected,vars);
    }

    public <T> T eval(Expr expr, Class<T> expected,ExprVars vars) {
        ExprEvaluator ev = new ExprEvaluator();
        ExprVars rowVars2=vars.copy();
        rowVars2.putAll(this.rowVars);
        return ev.evalExpr(expr, expected, rowVars2);
    }
}
