package net.thevpc.nmail.util;

import net.thevpc.nmail.NMailContext;
import net.thevpc.nmail.NMailDataSourceRow;
import net.thevpc.nmail.datasource.NMailDataSourceFilter;
import net.thevpc.nmail.expr.Expr;
import net.thevpc.nmail.expr.ExprVars;

public class MyNMailDataSourceFilter implements NMailDataSourceFilter {
    Expr expr;
    ExprVars vars;

    public MyNMailDataSourceFilter(Expr expr, ExprVars vars) {
        this.expr = expr;
        this.vars = vars;
    }

    @Override
    public boolean accept(NMailContext context, NMailDataSourceRow row) {
        if(expr==null){
            return true;
        }
        RowEvaluator re=new RowEvaluator(row);
        return re.eval(expr,boolean.class,vars);
    }

    public Expr toExpr() {
        return expr;
    }
}
