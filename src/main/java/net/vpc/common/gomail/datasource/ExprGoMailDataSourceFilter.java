/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail.datasource;

import java.util.Objects;
import net.vpc.common.gomail.GoMailContext;
import net.vpc.common.gomail.GoMailDataSourceRow;
import net.vpc.common.gomail.util.RowExprEvaluator;
import net.vpc.common.gomail.util.SerializedForm;
import net.vpc.upa.QLExpressionParser;
import net.vpc.upa.UPA;
import net.vpc.upa.expressions.Expression;
import net.vpc.upa.expressions.Select;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class ExprGoMailDataSourceFilter implements GoMailDataSourceFilter {

    private String expression;

    public ExprGoMailDataSourceFilter(String expression) {
        if (expression == null) {
            throw new NullPointerException(expression);
        }
        this.expression = expression;
    }

    @Override
    public SerializedForm serialize() {
        return new SerializedForm(ExprGoMailDataSourceFilter.class.getName(), expression);
    }

    public static ExprGoMailDataSourceFilter valueOf(SerializedForm serializedForm) {
        return new ExprGoMailDataSourceFilter(serializedForm.getValue());
    }

    @Override
    public boolean accept(GoMailContext context, GoMailDataSourceRow row) {
        RowExprEvaluator e = new RowExprEvaluator(context, row);
        QLExpressionParser parser = UPA.getBootstrap().getFactory().createObject(QLExpressionParser.class);
        String wexpr = expression;
        Expression exprObj = parser.parse(wexpr);
        if (exprObj instanceof Select) {
            Expression w = ((Select) exprObj).getWhere();
            wexpr = w==null?null:w.toString();
        }
        return wexpr == null || RowExprEvaluator.expressionToBoolean(e.evalString(wexpr), false);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.expression);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExprGoMailDataSourceFilter other = (ExprGoMailDataSourceFilter) obj;
        if (!Objects.equals(this.expression, other.expression)) {
            return false;
        }
        return true;
    }

}
