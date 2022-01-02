package net.thevpc.gomail.util;

import net.thevpc.gomail.GoMailContext;
import net.thevpc.gomail.GoMailDataSourceRow;
import net.thevpc.gomail.datasource.GoMailDataSourceFilter;
import net.thevpc.gomail.expr.Expr;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyGoMailDataSourceFilter implements GoMailDataSourceFilter {
    Expr expr;
    Map<String, Object> vars;

    public MyGoMailDataSourceFilter(Expr expr, Map<String, Object> vars) {
        this.expr = expr;
        this.vars = vars == null ? new LinkedHashMap<>() : vars;
    }

    @Override
    public boolean accept(GoMailContext context, GoMailDataSourceRow row) {
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
