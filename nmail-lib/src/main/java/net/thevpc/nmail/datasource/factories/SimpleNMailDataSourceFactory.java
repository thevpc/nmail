/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nmail.datasource.factories;

import net.thevpc.nmail.NMailDataSource;
import net.thevpc.nmail.NMailDataSourceFactory;
import net.thevpc.nmail.NScorableValue;
import net.thevpc.nmail.datasource.SimpleCsvNMailDataSource;
import net.thevpc.nmail.datasource.SimpleXlsNMailDataSource;
import net.thevpc.nmail.datasource.StringsNMailDataSource;
import net.thevpc.nmail.expr.Expr;
import net.thevpc.nmail.expr.StringExpr;

/**
 *
 * @author vpc
 */
public class SimpleNMailDataSourceFactory implements NMailDataSourceFactory {

    public SimpleNMailDataSourceFactory() {

    }

    public NScorableValue<NMailDataSource> createByFileType(String fileType, Expr arg) {
        if(fileType.equalsIgnoreCase(".csv")) {
            return new NScorableValue<NMailDataSource>() {
                @Override
                public int getScore() {
                    return 1;
                }

                @Override
                public NMailDataSource getValue() {
                    return new SimpleCsvNMailDataSource(arg);
                }
            };
        }
        if( fileType.equalsIgnoreCase("xls") || fileType.equalsIgnoreCase("xlsx")) {
            return new NScorableValue<NMailDataSource>() {
                @Override
                public int getScore() {
                    return 1;
                }

                @Override
                public NMailDataSource getValue() {
                    return new SimpleXlsNMailDataSource(arg);
                }
            };
        }
        return null;
    }
    @Override
    public NScorableValue<NMailDataSource> create(Expr arg) {
        if(arg instanceof StringExpr){
            String s=arg.asString();
            int ii = s.lastIndexOf('.');
            if(ii>=0){
                String t = s.substring(ii + 1);
                NScorableValue<NMailDataSource> u = createByFileType(t, arg);
                if(u!=null){
                    return u;
                }
            }
        }else if(arg.isFunction()){
            switch (arg.toFunction().getName()){
                case "string":
                {
                    return new NScorableValue<NMailDataSource>() {
                        @Override
                        public int getScore() {
                            return 1;
                        }

                        @Override
                        public NMailDataSource getValue() {
                            return new StringsNMailDataSource(arg.toFunction().getArgs()[0]);
                        }
                    };
                }
                default:{
                    return createByFileType(arg.toFunction().getName(),arg);
                }
            }
        }
        return null;
    }

}
