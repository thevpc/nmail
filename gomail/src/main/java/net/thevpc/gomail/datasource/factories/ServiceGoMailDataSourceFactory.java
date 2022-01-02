/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.gomail.datasource.factories;

import net.thevpc.gomail.GoMailDataSource;
import net.thevpc.gomail.GoMailDataSourceFactory;
import net.thevpc.gomail.SupportedValue;
import net.thevpc.gomail.expr.Expr;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 * @author vpc
 */
public class ServiceGoMailDataSourceFactory {

    private static ServiceGoMailDataSourceFactory INSTANCE;
    private List<GoMailDataSourceFactory> found = new ArrayList<>();

    {
        found.add(new SimpleGoMailDataSourceFactory());
    }

    public ServiceGoMailDataSourceFactory() {

    }

    public ServiceGoMailDataSourceFactory(ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        ServiceLoader<GoMailDataSourceFactory> loader = ServiceLoader.load(GoMailDataSourceFactory.class, classLoader);
        for (GoMailDataSourceFactory curr : loader) {
            found.add(curr);
        }
    }

    public static ServiceGoMailDataSourceFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (ServiceGoMailDataSourceFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServiceGoMailDataSourceFactory();
                }
            }
        }
        return INSTANCE;
    }

    public GoMailDataSource create(String type, Expr[] args) {
        SupportedValue<GoMailDataSource> best = null;
        int lvl = -1;
        for (GoMailDataSourceFactory f : found) {
            SupportedValue<GoMailDataSource> s = f.create(type, args);
            int newLvl = s.getSupportLevel();
            if (newLvl > 0) {
                if (newLvl > lvl) {
                    lvl = newLvl;
                    best = s;
                }
            }
        }
        if (best == null) {
            throw new NoSuchElementException("datasource factory not found for " + type);
        }
        return best.getValue();
    }
}
