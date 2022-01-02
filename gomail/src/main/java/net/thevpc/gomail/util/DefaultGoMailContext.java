/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.util;

import java.util.Map;

import net.thevpc.gomail.GoMailContext;
import net.thevpc.gomail.GoMailDataSource;
import net.thevpc.gomail.GoMailDataSourceRow;
import net.thevpc.gomail.GoMailProperties;
import net.thevpc.gomail.datasource.FilteredDataParserGoMailDataSource;
import net.thevpc.gomail.datasource.factories.ServiceGoMailDataSourceFactory;
import net.thevpc.gomail.expr.*;

/**
 * @author taha.bensalah@gmail.com
 */
public class DefaultGoMailContext implements GoMailContext {

    //    PropertyPlaceholderHelper h = new PropertyPlaceholderHelper("${", "}");
    private GoMailProperties properties;
    private Map<String, GoMailDataSource> datasources;
    private RowExprEvaluator e;
    private GoMailDataSourceRow row;

    public DefaultGoMailContext(Map<String, GoMailDataSource> datasources, GoMailProperties properties, GoMailDataSourceRow row) {
        this.row = row;
        this.properties = properties;
        this.datasources = datasources;
        e = new DefaultRowExprEvaluator(this, row);
    }

    @Override
    public Expr parse(String expr) {
        return new ExprParser(expr).parseStatementList();
    }

    @Override
    public GoMailDataSource buildDataSource(Expr expr, Map<String, Object> vars) {
        if (expr instanceof FctExpr) {
            FctExpr f = (FctExpr) expr;
            String n = f.getName();
            Expr[] a = f.getArgs();
            GoMailDataSource ss = ServiceGoMailDataSourceFactory.getInstance().create(n, a);
            ss.build(this,vars);
            return ss;
        }else if(expr instanceof OpExpr){
            OpExpr opExpr = (OpExpr) expr;
            if(opExpr.getOp()==TokenTType.PIPE){
                Expr[] args = opExpr.getArguments();
                Expr a = args[0];
                for (int i = 1; i <args.length; i++) {
                    if(i==args.length-1) {
                        FilteredDataParserGoMailDataSource ss = new FilteredDataParserGoMailDataSource(a, args[i]);
                        ss.build(this,vars);
                        return ss;
                    }else{
                        a=new OpExpr(TokenTType.PIPE,a,args[i]);
                    }
                }
                throw new IllegalArgumentException("impossible");
            }
        }
        throw new IllegalArgumentException("unsupported datasource from expression "+expr);
    }

    @Override
    public String eval(String expression, Map<String, Object> vars) {
        if (expression == null) {
            return "";
        }
        return GoMailUtils.replaceDollarPlaceHolders(expression, x -> e.evalExpressionString(x));
    }

    @Override
    public GoMailProperties getProperties() {
        return properties;
    }

    @Override
    public Map<String, GoMailDataSource> getRegisteredDataSources() {
        return datasources;
    }


}
