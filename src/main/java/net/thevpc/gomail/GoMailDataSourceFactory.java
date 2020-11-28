/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail;

import net.thevpc.gomail.datasource.CSVGoMailDataSource;
import net.thevpc.gomail.datasource.FixedWidthGoMailDataSource;
import net.thevpc.gomail.datasource.StringsGoMailDataSource;
import net.thevpc.gomail.datasource.SheetGoMailDataSource;
import net.thevpc.gomail.datasource.XMLGoMailDataSource;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import net.thevpc.common.io.FileUtils;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class GoMailDataSourceFactory {

    public static CSVGoMailDataSource forCSV(File file) {
        return new CSVGoMailDataSource(file);
    }

    public static CSVGoMailDataSource forCSV(URL url) {
        return new CSVGoMailDataSource(url);
    }

    public static CSVGoMailDataSource forCSV(String url) {
        return new CSVGoMailDataSource(url);
    }

    public static CSVGoMailDataSource forCSV(Reader reader) {
        return new CSVGoMailDataSource(reader);
    }

    public static CSVGoMailDataSource forCSV(InputStream inputStream) {
        return new CSVGoMailDataSource(inputStream);
    }

    public static FixedWidthGoMailDataSource forFixedWidth(File file) {
        return new FixedWidthGoMailDataSource(file);
    }

    public static FixedWidthGoMailDataSource forFixedWidth(URL url) {
        return new FixedWidthGoMailDataSource(url);
    }

    public static FixedWidthGoMailDataSource forFixedWidth(String url) {
        return new FixedWidthGoMailDataSource(url);
    }

    public static FixedWidthGoMailDataSource forFixedWidth(Reader reader) {
        return new FixedWidthGoMailDataSource(reader);
    }

    public static FixedWidthGoMailDataSource forFixedWidth(InputStream inputStream) {
        return new FixedWidthGoMailDataSource(inputStream);
    }

    public static XMLGoMailDataSource forXML(File file) {
        return new XMLGoMailDataSource(file);
    }

    public static XMLGoMailDataSource forXML(URL url) {
        return new XMLGoMailDataSource(url);
    }

    public static XMLGoMailDataSource forXML(String url) {
        return new XMLGoMailDataSource(url);
    }

    public static XMLGoMailDataSource forXML(Reader reader) {
        return new XMLGoMailDataSource(reader);
    }

    public static XMLGoMailDataSource forXML(InputStream inputStream) {
        return new XMLGoMailDataSource(inputStream);
    }

    public static SheetGoMailDataSource forSheet(File file) {
        return new SheetGoMailDataSource(file);
    }

    public static SheetGoMailDataSource forSheet(String url) {
        return new SheetGoMailDataSource(url);
    }

    public static SheetGoMailDataSource forSheet(URL url) {
        return new SheetGoMailDataSource(url);
    }

    public static SheetGoMailDataSource forSheet(InputStream inputStream) {
        return new SheetGoMailDataSource(inputStream);
    }

    public static GoMailDataSource forStrings(String[][] matrix, String[] columns) {
        return new StringsGoMailDataSource(matrix, columns);
    }

    public static GoMailDataSource forPattern(String url) {
        File file = FileUtils.toFileLenient(url);
        if (file == null) {
            throw new IllegalArgumentException("Unsupported");
        }
        String name = file.getName().toLowerCase();
        if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
            return forSheet(url);
        } else if (name.endsWith(".csv") || name.endsWith(".txt")) {
            return forCSV(url);
        } else if (name.endsWith(".fixed")) {
            return forFixedWidth(url);
        } else if (name.endsWith(".xml")) {
            return forXML(url);
        } else {
            throw new IllegalArgumentException("Unsupported");
        }
    }
}
