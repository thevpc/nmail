/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.upa;

import java.util.Objects;

import net.thevpc.gomail.GoMailContext;
import net.thevpc.gomail.GoMailDataSourceRow;
import net.thevpc.gomail.datasource.GoMailDataSourceFilter;
import net.thevpc.gomail.util.ExprList;
import net.thevpc.gomail.util.RowExprEvaluator;
import net.thevpc.gomail.util.SerializedForm;
import net.thevpc.upa.QLExpressionParser;
import net.thevpc.upa.UPA;
import net.thevpc.upa.expressions.Expression;
import net.thevpc.upa.expressions.Select;

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
        return new SerializedForm(
                new ExprList().addAll(
                    ExprList.createKeyValue("type", ExprGoMailDataSourceFilter.class.getName()),
                    ExprList.createKeyValue("value", expression)
                ));
    }

    public static ExprGoMailDataSourceFilter valueOf(SerializedForm serializedForm) {
        return new ExprGoMailDataSourceFilter(serializedForm.getValue());
    }

    @Override
    public boolean accept(GoMailContext context, GoMailDataSourceRow row) {
        RowExprEvaluator e = new UpaRowExprEvaluator(context, row);
        QLExpressionParser parser = UPA.getBootstrap().getFactory().createObject(QLExpressionParser.class);
        String wexpr = expression;
        Expression exprObj = parser.parse(wexpr);
        if (exprObj instanceof Select) {
            Expression w = ((Select) exprObj).getWhere();
            wexpr = w==null?null:w.toString();
        }
        return wexpr == null || UpaRowExprEvaluator.expressionToBoolean(e.evalExpressionString(wexpr), false);
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
