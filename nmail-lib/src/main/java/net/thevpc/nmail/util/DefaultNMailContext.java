/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail.util;

import java.util.Map;

import net.thevpc.nmail.*;
import net.thevpc.nmail.datasource.factories.ServiceNMailDataSourceFactory;
import net.thevpc.nmail.expr.*;

/**
 * @author taha.bensalah@gmail.com
 */
public class DefaultNMailContext implements NMailContext {

    //    PropertyPlaceholderHelper h = new PropertyPlaceholderHelper("${", "}");
    private NMailProperties properties;
    private Map<String, NMailDataSource> datasources;
    private RowExprEvaluator exprEvaluator;
    private NMailDataSourceRow row;
    private NTracker tracker;
    private boolean logMessage;

    public DefaultNMailContext(Map<String, NMailDataSource> datasources, NMailProperties properties, NMailDataSourceRow row, NTracker tracker, boolean logMessage) {
        this.row = row;
        this.properties = properties;
        this.datasources = datasources;
        this.tracker = tracker;
        this.exprEvaluator = new DefaultRowExprEvaluator(this, row);
        this.logMessage = logMessage;
    }

    @Override
    public boolean isLogMessage() {
        return logMessage;
    }

    @Override
    public NTracker tracker() {
        return tracker;
    }

    @Override
    public Expr parse(String expr) {
        return new ExprParser(expr).parseStatementList();
    }

    @Override
    public NMailDataSource buildDataSource(Expr expr, ExprVars vars) {
        if (expr.isWord()) {
            NMailDataSource ds = datasources.get(expr.asString());
            if (ds != null) {
                ds.build(this, vars);
                return ds;
            }
        }
        NMailDataSource ss = ServiceNMailDataSourceFactory.getInstance().create(expr);
        ss.build(this, vars);
        return ss;
    }

    @Override
    public String eval(String expression, ExprVars vars) {
        if (expression == null) {
            return "";
        }
        return NMailUtils.replaceDollarPlaceHolders(expression, x -> exprEvaluator.evalExpressionString(x));
    }

    @Override
    public NMailProperties getProperties() {
        return properties;
    }

    @Override
    public Map<String, NMailDataSource> getRegisteredDataSources() {
        return datasources;
    }


}
