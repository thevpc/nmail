/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.gomail.datasource.factories;

import net.thevpc.gomail.GoMailDataSource;
import net.thevpc.gomail.GoMailDataSourceFactory;
import net.thevpc.gomail.SupportedValue;
import net.thevpc.gomail.datasource.FilteredDataParserGoMailDataSource;
import net.thevpc.gomail.datasource.SimpleCsvGoMailDataSource;
import net.thevpc.gomail.expr.Expr;
import net.thevpc.gomail.util.SerializedForm;

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
public class SimpleGoMailDataSourceFactory implements GoMailDataSourceFactory {

    private List<GoMailDataSourceFactory> found = new ArrayList<>();

    public SimpleGoMailDataSourceFactory() {

    }

    @Override
    public SupportedValue<GoMailDataSource> create(String type, Expr[] args) {
        switch (type){
            case "csv":{
                return new SupportedValue<GoMailDataSource>() {
                    @Override
                    public int getSupportLevel() {
                        return 1;
                    }

                    @Override
                    public GoMailDataSource getValue() {
                        return new SimpleCsvGoMailDataSource(args);
                    }
                };
            }
            case "filter":{
                return new SupportedValue<GoMailDataSource>() {
                    @Override
                    public int getSupportLevel() {
                        return 1;
                    }

                    @Override
                    public GoMailDataSource getValue() {
                        return new FilteredDataParserGoMailDataSource(args[0],args[1]);
                    }
                };
            }
        }
        return null;
    }

}
