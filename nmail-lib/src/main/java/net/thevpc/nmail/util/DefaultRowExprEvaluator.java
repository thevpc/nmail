/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nmail.util;

import net.thevpc.nmail.NMailContext;
import net.thevpc.nmail.NMailDataSourceRow;

import java.util.HashMap;

/**
 * @author vpc
 */
public class DefaultRowExprEvaluator implements RowExprEvaluator {

    private NMailDataSourceRow sourceRow;
    private NMailContext context;

    public DefaultRowExprEvaluator(NMailContext context, final NMailDataSourceRow row) {
        this.sourceRow = row;
        this.context = context;
    }

    @Override
    public String evalExpressionString(String placeholderName) {
        RowEvaluator ev = new RowEvaluator(sourceRow);
        return ev.eval(placeholderName, String.class, new NMailContextExprVars(context,new HashMap<>()));
    }

}
