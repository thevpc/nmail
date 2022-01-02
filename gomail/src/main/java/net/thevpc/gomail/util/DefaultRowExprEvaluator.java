/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.gomail.util;

import net.thevpc.gomail.GoMailContext;
import net.thevpc.gomail.GoMailDataSourceRow;

import java.util.HashMap;

/**
 * @author vpc
 */
public class DefaultRowExprEvaluator implements RowExprEvaluator {

    private GoMailDataSourceRow sourceRow;
    private GoMailContext context;

    public DefaultRowExprEvaluator(GoMailContext context, final GoMailDataSourceRow row) {
        this.sourceRow = row;
        this.context = context;
    }

    @Override
    public String evalExpressionString(String placeholderName) {
        RowEvaluator ev = new RowEvaluator(sourceRow);
        return ev.eval(placeholderName, String.class,new HashMap<>());
    }

}
