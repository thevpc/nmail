/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.gomail.util;

import net.thevpc.gomail.GoMailContext;
import net.thevpc.gomail.GoMailDataSourceRow;

/**
 *
 * @author vpc
 */
public class DefaultRowExprEvaluator implements RowExprEvaluator {

    private GoMailDataSourceRow sourceRow;

    public DefaultRowExprEvaluator(GoMailContext context, final GoMailDataSourceRow row) {
        this.sourceRow = row;
    }

    @Override
    public String evalExpressionString(String placeholderName) {
        return placeholderName;
    }

}
