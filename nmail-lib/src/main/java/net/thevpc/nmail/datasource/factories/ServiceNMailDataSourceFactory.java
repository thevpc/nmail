/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nmail.datasource.factories;

import net.thevpc.nmail.NMailDataSource;
import net.thevpc.nmail.NMailDataSourceFactory;
import net.thevpc.nmail.SupportedValue;
import net.thevpc.nmail.datasource.FilteredDataParserNMailDataSource;
import net.thevpc.nmail.expr.Expr;
import net.thevpc.nmail.expr.OpExpr;
import net.thevpc.nmail.expr.TokenTType;

import java.util.*;

/**
 * @author vpc
 */
public class ServiceNMailDataSourceFactory {

    private static ServiceNMailDataSourceFactory INSTANCE;
    private List<NMailDataSourceFactory> found = new ArrayList<>();

    {
        found.add(new SimpleNMailDataSourceFactory());
    }

    public ServiceNMailDataSourceFactory() {

    }

    public ServiceNMailDataSourceFactory(ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        ServiceLoader<NMailDataSourceFactory> loader = ServiceLoader.load(NMailDataSourceFactory.class, classLoader);
        for (NMailDataSourceFactory curr : loader) {
            found.add(curr);
        }
    }

    public static ServiceNMailDataSourceFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (ServiceNMailDataSourceFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServiceNMailDataSourceFactory();
                }
            }
        }
        return INSTANCE;
    }

    public NMailDataSource create(Expr arg) {
        if(arg.isOp(TokenTType.PIPE)){
            OpExpr op = arg.toOp();
            Expr[] arguments = op.getArguments();
            NMailDataSource filter = new FilteredDataParserNMailDataSource(arguments[0], arguments[1]);
            for (int i = 2; i < arguments.length; i++) {
                filter =new FilteredDataParserNMailDataSource(filter.toExpr(), arguments[i]);
            }
            return filter;
        }

        SupportedValue<NMailDataSource> best = null;
        int lvl = -1;
        for (NMailDataSourceFactory f : found) {
            SupportedValue<NMailDataSource> s = f.create(arg);
            if(s!=null) {
                int newLvl = s.getSupportLevel();
                if (newLvl > 0) {
                    if (newLvl > lvl) {
                        lvl = newLvl;
                        best = s;
                    }
                }
            }
        }
        if (best == null) {
            throw new NoSuchElementException("datasource factory not found for " + arg);
        }
        return best.getValue();
    }
}
