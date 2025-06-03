/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail;

import net.thevpc.nmail.expr.Expr;
import net.thevpc.nmail.expr.ExprVars;

import java.util.Map;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface NMailContext {

    NTracker tracker();
    Expr parse(String expr);

    NMailDataSource buildDataSource(Expr expr, ExprVars vars);

    public String eval(String expr, ExprVars vars);

    public NMailProperties getProperties();

    public Map<String, NMailDataSource> getRegisteredDataSources();

    boolean isLogMessage();
}
