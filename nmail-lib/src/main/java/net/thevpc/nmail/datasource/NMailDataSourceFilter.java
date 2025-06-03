/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail.datasource;

import net.thevpc.nmail.NMailContext;
import net.thevpc.nmail.NMailDataSourceRow;
import net.thevpc.nmail.expr.Expr;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface NMailDataSourceFilter {

    public boolean accept(NMailContext context, NMailDataSourceRow row);

    public Expr toExpr();
}
