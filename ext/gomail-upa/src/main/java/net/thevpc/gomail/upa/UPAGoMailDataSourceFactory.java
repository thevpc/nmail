/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.gomail.upa;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import net.thevpc.gomail.GoMailDataSource;
import net.thevpc.gomail.GoMailDataSourceFactory;

/**
 *
 * @author vpc
 */
public class UPAGoMailDataSourceFactory implements GoMailDataSourceFactory {

    @Override
    public int getSupportLevel(String type) {
        switch (type) {
            case "csv":
            case "fixed-width":
            case "fixed":
            case "sheet":
            case "xls":
            case "xlsx":
            case "xml":
                return 2;
        }
        return -1;
    }

    @Override
    public GoMailDataSource create(String type, URL source) {
        switch (type) {
            case "csv":
                return new CSVGoMailDataSource(source);
            case "fixed-width":
            case "fixed":
                return new FixedWidthGoMailDataSource(source);
            case "sheet":
            case "xls":
            case "xlsx":
                return new SheetGoMailDataSource(source);
            case "xml":
                return new XMLGoMailDataSource(source);
        }
        throw new UnsupportedOperationException("Not supported: " + type);
    }

    @Override
    public GoMailDataSource create(String type, Reader source) {
        switch (type) {
            case "csv":
                return new CSVGoMailDataSource(source);
            case "fixed-width":
            case "fixed":
                return new FixedWidthGoMailDataSource(source);
            case "sheet":
            case "xls":
            case "xlsx":{
                //unsupported
                break;
            }
            case "xml":
                return new XMLGoMailDataSource(source);
        }
        throw new UnsupportedOperationException("Not supported: " + type);
    }

    @Override
    public GoMailDataSource create(String type, InputStream source) {
        switch (type) {
            case "csv":
                return new CSVGoMailDataSource(source);
            case "fixed-width":
            case "fixed":
                return new FixedWidthGoMailDataSource(source);
            case "sheet":
            case "xls":
            case "xlsx":
                return new SheetGoMailDataSource(source);
            case "xml":
                return new XMLGoMailDataSource(source);
        }
        throw new UnsupportedOperationException("Not supported: " + type);
    }

    @Override
    public GoMailDataSource create(String type, File file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
