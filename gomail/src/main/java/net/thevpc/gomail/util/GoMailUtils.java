package net.thevpc.gomail.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.thevpc.gomail.GoMailDataSource;
import net.thevpc.gomail.ServiceGoMailDataSourceFactory;
import net.thevpc.gomail.datasource.StringsGoMailDataSource;

/**
 * Created by vpc on 7/5/16.
 */
public class GoMailUtils {

    private static final Pattern DOLLAR_PLACE_HOLDER_PATTERN = Pattern.compile("[$][{](?<name>([^}]+))[}]");

    public static String replaceDollarPlaceHolders(String s, Function<String, String> converter) {
//        return replacePlaceHolders(s, "${", "}", converter);
        // return replacePlaceHolders(s, "${", "}", converter);
        //faster default implementation
        Matcher matcher = DOLLAR_PLACE_HOLDER_PATTERN.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group("name");
            String x = converter.apply(name);
            if (x == null) {
                x = "${" + name + "}";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String probeContentType(String fileName) {
        try {
            return Files.probeContentType(Paths.get(fileName));
        } catch (IOException ex) {
            //ignore
        }
        return null;
    }

    public static boolean isTextPlainContentType(String contentType) {
        if (contentType != null) {
            for (String s : contentType.split(";")) {
                if (s.trim().equals("text/plain")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isTextHtmlContentType(String contentType) {
        if (contentType != null) {
            for (String s : contentType.split(";")) {
                if (s.trim().equals("text/html")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isTextContentType(String contentType) {
        if (contentType != null) {
            for (String s : contentType.split(";")) {
                if (s.trim().startsWith("text/")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static File toFileLenient(String path) {
        if (path == null || path.length() == 0) {
            return null;
        }
        if (path.startsWith("/") || path.startsWith("\\")) {
            return new File(path);
        }
        if (path.toLowerCase().startsWith("file:")) {
            Path pa;
            try {
                pa = Paths.get(new URL(path).toURI());
            } catch (Exception ex) {
                return new File(path.substring("file:".length()));
            }
            return pa.toFile();
        }
        if (path.indexOf(':') == 1) {
            for (int i = 0; i < 26; i++) {
                if (path.toLowerCase().startsWith(('a') + ":")) {
                    //windows drive letter
                    return new File(path);
                }
            }
        }
        return null;
    }

    public static GoMailDataSource forType(String type, File file) {
        return ServiceGoMailDataSourceFactory.getInstance().create(type, file);
    }

    public static GoMailDataSource forType(String type, Reader file) {
        return ServiceGoMailDataSourceFactory.getInstance().create(type, file);
    }

    public static GoMailDataSource forType(String type, InputStream stream) {
        return ServiceGoMailDataSourceFactory.getInstance().create(type, stream);
    }

    public static GoMailDataSource forType(String type, URL file) {
        return ServiceGoMailDataSourceFactory.getInstance().create(type, file);
    }

    public static GoMailDataSource forCSV(File file) {
        return forType("csv", file);
    }

    public static GoMailDataSource forCSV(URL url) {
        return forType("csv", url);
    }

    public static GoMailDataSource forCSV(String url) {
        return forType("csv", toURL(url));
    }

    public static GoMailDataSource forCSV(Reader reader) {
        return forType("csv", reader);
    }

    public static GoMailDataSource forCSV(InputStream inputStream) {
        return forType("csv", inputStream);
    }

    public static GoMailDataSource forFixedWidth(File file) {
        return forType("fixed-width", file);
    }

    public static GoMailDataSource forFixedWidth(URL url) {
        return forType("fixed-width", url);
    }

    public static GoMailDataSource forFixedWidth(String url) {
        return forType("fixed-width", toURL(url));
    }

    public static GoMailDataSource forFixedWidth(Reader reader) {
        return forType("fixed-width", reader);
    }

    public static GoMailDataSource forFixedWidth(InputStream inputStream) {
        return forType("fixed-width", inputStream);
    }

    public static GoMailDataSource forXML(File file) {
        return forType("xml", file);
    }

    public static GoMailDataSource forXML(URL url) {
        return forType("xml", url);
    }

    public static GoMailDataSource forXML(String url) {
        return forType("xml", toURL(url));
    }

    public static GoMailDataSource forXML(Reader reader) {
        return forType("xml", reader);
    }

    public static GoMailDataSource forXML(InputStream inputStream) {
        return forType("xml", inputStream);
    }

    public static GoMailDataSource forSheet(File file) {
        return forType("xls", file);
    }

    public static GoMailDataSource forSheet(String url) {
        return forType("xls", toURL(url));
    }

    public static GoMailDataSource forSheet(URL url) {
        return forType("xls", url);
    }

    public static GoMailDataSource forSheet(InputStream inputStream) {
        return forType("xls", inputStream);
    }

    public static GoMailDataSource forStrings(String[][] matrix, String[] columns) {
        return new StringsGoMailDataSource(matrix, columns);
    }

    public static GoMailDataSource forPattern(String url) {
        File file = toFileLenient(url);
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

    private static URL toURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static byte[] loadByteArray(InputStream is) {

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

    }

    public static int indexOfRegexpStart(String value, String regexp) {
        Pattern t = Pattern.compile(regexp);
        Matcher matcher = t.matcher(value);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    public static int indexOfRegexpEnd(String value, String regexp) {
        Pattern t = Pattern.compile(regexp);
        Matcher matcher = t.matcher(value);
        if (matcher.find()) {
            return matcher.end();
        }
        return -1;
    }
}
