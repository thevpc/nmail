/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail;

import net.thevpc.gomail.expr.Expr;

import java.util.Map;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface GoMailContext {

    Expr parse(String expr);

    GoMailDataSource buildDataSource(Expr expr, Map<String,Object> vars);

    public String eval(String expr, Map<String, Object> vars);

    public GoMailProperties getProperties();

    public Map<String, GoMailDataSource> getRegisteredDataSources();
}
