/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.gomail;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 *
 * @author vpc
 */
public class ServiceGoMailDataSourceFactory implements GoMailDataSourceFactory {

    private static GoMailDataSourceFactory INSTANCE;

    public static GoMailDataSourceFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (ServiceGoMailDataSourceFactory.class) {
                if(INSTANCE==null){
                    INSTANCE=new ServiceGoMailDataSourceFactory();
                }
            }
        }
        return INSTANCE;
    }
    
    private List<GoMailDataSourceFactory> found = new ArrayList<>();

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

    public GoMailDataSourceFactory getFactory(String type) {
        GoMailDataSourceFactory best = null;
        int lvl = -1;
        for (GoMailDataSourceFactory f : found) {
            final int s = f.getSupportLevel(type);
            if (s > 0) {
                if (s > lvl) {
                    lvl = s;
                    best = f;
                }
            }
        }
        if (best == null) {
            throw new NoSuchElementException("datasource factory not found for " + type);
        }
        return best;
    }

    @Override
    public int getSupportLevel(String type) {
        GoMailDataSourceFactory best;
        int lvl = -1;
        for (GoMailDataSourceFactory f : found) {
            final int s = f.getSupportLevel(type);
            if (s > 0) {
                if (s > lvl) {
                    lvl = s;
                    best = f;
                }
            }
        }
        return lvl;
    }

    @Override
    public GoMailDataSource create(String type, URL url) {
        return getFactory(type).create(type, url);
    }

    @Override
    public GoMailDataSource create(String type, Reader reader) {
        return getFactory(type).create(type, reader);
    }

    @Override
    public GoMailDataSource create(String type, InputStream stream) {
        return getFactory(type).create(type, stream);
    }

    @Override
    public GoMailDataSource create(String type, File file) {
        return getFactory(type).create(type, file);
    }

}
