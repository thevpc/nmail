//package net.thevpc.gomail.util;
//
//import net.thevpc.gomail.GoMailDataSource;
//import net.thevpc.gomail.datasource.StringsGoMailDataSource;
//import net.thevpc.gomail.datasource.factories.ServiceGoMailDataSourceFactory;
//
//import java.io.File;
//import java.io.InputStream;
//import java.io.Reader;
//import java.net.URL;
//
//public class GoMailDeprecatedUtils {
//    public static GoMailDataSource forType(String type, File file) {
//        return ServiceGoMailDataSourceFactory.getInstance().create(type, file);
//    }
//
//    public static GoMailDataSource forType(String type, Reader file) {
//        return ServiceGoMailDataSourceFactory.getInstance().create(type, file);
//    }
//
//    public static GoMailDataSource forType(String type, InputStream stream) {
//        return ServiceGoMailDataSourceFactory.getInstance().create(type, stream);
//    }
//
//    public static GoMailDataSource forType(String type, URL file) {
//        return ServiceGoMailDataSourceFactory.getInstance().create(type, file);
//    }
//
//    public static GoMailDataSource forCSV(File file) {
//        return forType("csv", file);
//    }
//
//    public static GoMailDataSource forCSV(URL url) {
//        return forType("csv", url);
//    }
//
//    public static GoMailDataSource forCSV(String url) {
//        return forType("csv", GoMailUtils.toURL(url));
//    }
//
//    public static GoMailDataSource forCSV(Reader reader) {
//        return forType("csv", reader);
//    }
//
//    public static GoMailDataSource forCSV(InputStream inputStream) {
//        return forType("csv", inputStream);
//    }
//
//    public static GoMailDataSource forFixedWidth(File file) {
//        return forType("fixed-width", file);
//    }
//
//    public static GoMailDataSource forFixedWidth(URL url) {
//        return forType("fixed-width", url);
//    }
//
//    public static GoMailDataSource forFixedWidth(String url) {
//        return forType("fixed-width", GoMailUtils.toURL(url));
//    }
//
//    public static GoMailDataSource forFixedWidth(Reader reader) {
//        return forType("fixed-width", reader);
//    }
//
//    public static GoMailDataSource forFixedWidth(InputStream inputStream) {
//        return forType("fixed-width", inputStream);
//    }
//
//    public static GoMailDataSource forXML(File file) {
//        return forType("xml", file);
//    }
//
//    public static GoMailDataSource forXML(URL url) {
//        return forType("xml", url);
//    }
//
//    public static GoMailDataSource forXML(String url) {
//        return forType("xml", GoMailUtils.toURL(url));
//    }
//
//    public static GoMailDataSource forXML(Reader reader) {
//        return forType("xml", reader);
//    }
//
//    public static GoMailDataSource forXML(InputStream inputStream) {
//        return forType("xml", inputStream);
//    }
//
//    public static GoMailDataSource forSheet(File file) {
//        return forType("xls", file);
//    }
//
//    public static GoMailDataSource forSheet(String url) {
//        return forType("xls", GoMailUtils.toURL(url));
//    }
//
//    public static GoMailDataSource forSheet(URL url) {
//        return forType("xls", url);
//    }
//
//    public static GoMailDataSource forSheet(InputStream inputStream) {
//        return forType("xls", inputStream);
//    }
//
//    public static GoMailDataSource forStrings(String[][] matrix, String[] columns) {
//        return new StringsGoMailDataSource(matrix, columns);
//    }
//
//    public static GoMailDataSource forPattern(String url) {
//        File file = GoMailUtils.toFileLenient(url);
//        if (file == null) {
//            throw new IllegalArgumentException("Unsupported");
//        }
//        String name = file.getName().toLowerCase();
//        if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
//            return forSheet(url);
//        } else if (name.endsWith(".csv") || name.endsWith(".txt")) {
//            return forCSV(url);
//        } else if (name.endsWith(".fixed")) {
//            return forFixedWidth(url);
//        } else if (name.endsWith(".xml")) {
//            return forXML(url);
//        } else {
//            throw new IllegalArgumentException("Unsupported");
//        }
//    }
//}
