package net.thevpc.gomail.util;

import net.thevpc.gomail.GoMailDataSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vpc on 7/5/16.
 */
public class GoMailUtils {

    private static final Pattern DOLLAR_PLACE_HOLDER_PATTERN = Pattern.compile("[$][{](?<name>([^}]+))[}]");
    private static final char[] hexDigit = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

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

    public static boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    public static URL toURL(String url) {
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

    public static String escapeStringWithDoubleQuotes(String s) {
        String q = escapeString(s);
        return "\""+q+"\"";
    }

    public static String[][] getDataTable(GoMailDataSource ds) {
        String[][] c=new String[ds.getRowCount()][];
        for (int i = 0; i < c.length; i++) {
            c[i]=new String[ds.getColumnCount()];
            for (int j = 0; j < c[i].length; j++) {
                c[i][j]=ds.getCell(i,j);
            }
        }
        return c;
    }

    public static String[] getColumns(GoMailDataSource ds) {
        String[] c=new String[ds.getColumnCount()];
        for (int i = 0; i < c.length; i++) {
            c[i]=ds.getColumn(i);
        }
        return c;
    }

    public static String escapeStringWithSimpleQuotes(String s) {
        String q = escapeString(s);
        return "'"+q+"'";
    }

    public static String escapeString(String s) {
        if (s == null) {
            return "null";
        } else {
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '\'':
                    case '\\': {
                        sb.append('\\').append(c);
                        break;
                    }
                    case '\n': {
                        sb.append("\\n");
                        break;
                    }
                    case '\t': {
                        sb.append("\\t");
                        break;
                    }
                    case '\r': {
                        sb.append("\\r");
                        break;
                    }
                    case '\f': {
                        sb.append("\\f");
                        break;
                    }
                    default: {
                        if ((c < 0x0020) || (c > 0x007e)) {
                            sb.append('\\');
                            sb.append('u');
                            sb.append(toHex((c >> 12) & 0xF));
                            sb.append(toHex((c >> 8) & 0xF));
                            sb.append(toHex((c >> 4) & 0xF));
                            sb.append(toHex(c & 0xF));
                        } else {
                            sb.append(c);
                        }
                    }
                }
            }
            return sb.toString();
        }
    }

    private static char toHex(int nibble) {
        return hexDigit[nibble & 0xf];
    }
}
