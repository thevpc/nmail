/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail.modules;

import net.vpc.common.gomail.*;
import net.vpc.common.gomail.datasource.CSVGoMailDataSource;
import net.vpc.common.gomail.datasource.FixedWidthGoMailDataSource;
import net.vpc.common.gomail.datasource.StringsGoMailDataSource;
import net.vpc.common.gomail.datasource.SheetGoMailDataSource;
import net.vpc.common.gomail.datasource.XMLGoMailDataSource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.common.gomail.datasource.ExprGoMailDataSourceFilter;
import net.vpc.common.gomail.datasource.GoMailDataSourceFilter;
import net.vpc.common.gomail.datasource.FilteredDataParserGoMailDataSource;
import net.vpc.common.gomail.util.GoMailUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.io.InputStreamSource;
import net.vpc.common.io.OutputStreamSource;
import net.vpc.common.io.PathInfo;
import net.vpc.common.gomail.util.SerializedForm;
import net.vpc.common.gomail.util.SerializedFormConfig;
import net.vpc.common.strings.StringUtils;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class GoMailModuleSerializer {

    public static final String SER_HEADER = "#mimetype=application/go-mail";

    public static final SerializedFormConfig GoMailDataSourceSerializedFormConfig
            = new SerializedFormConfig()
            .addAlias("sheet", SheetGoMailDataSource.class.getName())
            .addAlias("array", StringsGoMailDataSource.class.getName())
            .addAlias("fixed", FixedWidthGoMailDataSource.class.getName())
            .addAlias("csv", CSVGoMailDataSource.class.getName())
            .addAlias("xml", XMLGoMailDataSource.class.getName())
            .addAlias("default", FilteredDataParserGoMailDataSource.class.getName())
            .addImport(GoMailDataSource.class.getPackage().getName());

    public static final SerializedFormConfig GoMailDataSourcefilterSerializedFormConfig
            = new SerializedFormConfig()
            .addAlias("expr", ExprGoMailDataSourceFilter.class.getName())
            .addAlias("default", ExprGoMailDataSourceFilter.class.getName())
            .addImport(ExprGoMailDataSourceFilter.class.getPackage().getName());

    public void write(GoMail mail, GoMailFormat format, File file) throws IOException {
        write(mail, format, IOUtils.toOutputStreamSource(file));
    }
    public void write(GoMailMessage mail, GoMailFormat format, File file) throws IOException {
        write(mail, format, IOUtils.toOutputStreamSource(file));
    }

    public String gomailToString(GoMail mail) {
        try {
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            write(mail, GoMailFormat.TEXT, s);
            return new String(s.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    public String gomailToString(GoMailMessage mail) {
        try {
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            write(mail, GoMailFormat.TEXT, s);
            return new String(s.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public GoMail gomailFromString(String mail) throws IOException {
        ByteArrayInputStream s = new ByteArrayInputStream(mail.getBytes());
        return read(GoMailFormat.TEXT, s);
    }

    public void write(GoMail mail, GoMailFormat format, OutputStreamSource file) throws IOException {
        OutputStream out = null;
        try {
            out = file.open();
            write(mail, format, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public void write(GoMailMessage mail, GoMailFormat format, OutputStreamSource file) throws IOException {
        OutputStream out = null;
        try {
            out = file.open();
            write(mail, format, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public void write(GoMail mail, GoMailFormat format, OutputStream stream) throws IOException {
        switch (format) {
            case TEXT: {
                writeText(mail, stream);
                return;
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }
    public void write(GoMailMessage mail, GoMailFormat format, OutputStream stream) throws IOException {
        switch (format) {
            case TEXT: {
                writeText(mail, stream);
                return;
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }

    public GoMail read(GoMailFormat format, File file) throws IOException {
        return read(format, IOUtils.toInputStreamSource(file));
    }

    public GoMail read(GoMailFormat format, InputStreamSource source) throws IOException {
        InputStream in = null;
        try {
            in = source.open();
            GoMail m = read(format, in);
            Object f = source.getSource();
            PathInfo pathInfo = PathInfo.create(f);
            if (pathInfo != null) {
                if (pathInfo.getBaseName() != null) {
                    m.getProperties().put("basename", pathInfo.getBaseName());
                }
                if (pathInfo.getDirName() != null) {
                    m.getProperties().put("dirname", pathInfo.getDirName());
                }
                if (pathInfo.getPathName() != null) {
                    m.getProperties().put("pathname", pathInfo.getPathName());
                }
            }
            return m;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public GoMail read(GoMailFormat format, InputStream stream) throws IOException {
        switch (format) {
            case TEXT: {
                return readText(new BufferedReader(new InputStreamReader(stream)));
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }

    private GoMail readText(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (!SER_HEADER.equals((line == null ? "" : line).trim())) {
            throw new IllegalArgumentException("Expected " + SER_HEADER);
        }
        GoMail m = new GoMail();
        String pushbackLine = null;
        while (true) {
            line = pushbackLine == null ? reader.readLine() : pushbackLine;
            pushbackLine = null;
            if (line != null) {
                line = line.trim();
                if (!line.startsWith("#") && line.length() > 0) {
                    String[] u = line.toLowerCase().split("(:| )+");
                    if (u.length > 1) {
                        String cmd = u[0];
                        int i = StringUtils.indexOfRegexpEnd(line, "(:| )+");
                        String propValue = line.substring(i).trim();
                        if (cmd.equals("from")) {
                            m.from(lineUnEscape(propValue));
                        } else if (cmd.equals("to")) {
                            m.to(lineUnEscape(propValue));
                        } else if (cmd.equals("toeach")) {
                            m.toeach(lineUnEscape(propValue));
                        } else if (cmd.equals("cc")) {
                            m.cc(lineUnEscape(propValue));
                        } else if (cmd.equals("bcc")) {
                            m.bcc(lineUnEscape(propValue));
                        } else if (cmd.equals("subject")) {
                            m.subject(lineUnEscape(propValue));
                        } else if (cmd.equals("property")) {
                            int eq = propValue.indexOf('=');
                            m.getProperties().put(
                                    lineUnEscape(propValue.substring(0, eq)), lineUnEscape(propValue.substring(eq + 1))
                            );
                        } else if (cmd.equals("repeat")) {
                            m.repeatDatasource(deserializeDataSource(propValue));
                        } else if (cmd.equals("datasource")) {
                            int dots = propValue.indexOf('=');
                            if (dots <= 0) {
                                throw new IllegalArgumentException("missing var name in datasource : " + propValue);
                            }
                            String name = lineUnEscape(propValue.substring(0, dots));
                            String serVal = propValue.substring(dots + 1);
                            m.namedDataSources().put(name, deserializeDataSource(serVal));
                        } else if (cmd.equals("simulate")) {
                            m.setSimulate(Boolean.valueOf(propValue));
                        } else if (cmd.equals("object") || cmd.equals("attachment") || cmd.equals("footer") || cmd.equals("header")) {
                            GoMailBodyPosition pos = GoMailBodyPosition.valueOf(cmd.toUpperCase());
                            boolean expandable = true;
                            String contentType = GoMail.HTML_CONTENT_TYPE;
                            boolean path = false;
//                            boolean content = true;
                            boolean base64 = false;
                            int order = 0;
                            for (String pp : propValue.split(" +")) {
                                pp = pp.trim();
                                if (pp.length() > 0 && !pp.equals(";")) {
                                    if (pp.equals("expandable")) {
                                        expandable = true;
                                    } else if (pp.equals("!expandable")) {
                                        expandable = false;
                                    } else if (pp.equals("path")) {
                                        path = true;
                                    } else if (pp.equals("base64")) {
                                        base64 = true;
                                    } else if (pp.startsWith("order=")) {
                                        order = Integer.parseInt(pp.substring("order=".length()));
                                    } else if (pp.contains("/")) {
                                        contentType = pp;
                                    } else {
                                        throw new IllegalArgumentException("Unsupported " + pp);
                                    }
                                }
                            }
                            if (path) {
                                if (base64) {
                                    throw new IllegalArgumentException("Cannot read path as base 64");
                                } else {
                                    String pathValue = null;
                                    while (true) {
                                        line = reader.readLine();
                                        if (line == null) {
                                            throw new IllegalArgumentException("Expected path");
                                        } else if (line.trim().startsWith("#")) {
                                            //ignore
                                        } else {
                                            pathValue = line.trim();
                                            break;
                                        }
                                    }
                                    m.body(new URL(pathValue), contentType);
                                    GoMailBody last = m.body().get(m.body().size() - 1);
                                    last.setOrder(order);
                                    last.setExpandable(expandable);
                                    last.setPosition(pos);
                                }
                            } else {
                                if (base64) {
                                    StringBuilder encoded = new StringBuilder();
                                    while (true) {
                                        line = reader.readLine();
                                        if (line == null) {
                                            break;
                                        } else if (line.trim().startsWith("#")) {
                                            //ignore
                                        } else if (line.contains(":")) {
                                            pushbackLine = line;
                                            break;
                                        } else {
                                            encoded.append(line);
                                        }
                                    }
                                    m.body(Base64.getDecoder().decode(encoded.toString()), contentType, pos);
                                } else {
                                    StringBuilder fullText = new StringBuilder();
                                    boolean first = true;
                                    while (true) {
                                        line = reader.readLine();
                                        if (line == null) {
                                            break;
                                        } else if (line.trim().equals("<<end>>")) {
                                            break;
                                        } else {
                                            if (first) {
                                                first = false;
                                            } else {
                                                fullText.append("\n");
                                            }
                                            fullText.append(line);
                                        }
                                    }
                                    m.body(fullText, contentType, pos);
                                }
                                GoMailBody last = m.body().get(m.body().size() - 1);
                                last.setContentType(contentType);
                                last.setOrder(order);
                                last.setExpandable(expandable);
                                last.setPosition(pos);
                            }
                        } else {
                            throw new IllegalArgumentException("Unexpected " + line);
                        }
                    }
                }
            } else {
                break;
            }
        }
        return m;
    }

    

    private void writeText(GoMail mail, OutputStream stream) throws IOException {
        PrintStream out = (stream instanceof PrintStream) ? ((PrintStream) stream) : new PrintStream(stream);
        out.println(SER_HEADER);
        {
            String f = mail.from();
            if (f != null) {
                out.print("from : ");
                out.print(lineEscape(f));
                out.println();
            }
        }
        {
            Set<String> f = mail.to();
            if (f != null && f.size() > 0) {
                for (String v : f) {
                    out.print("to : ");
                    out.print(lineEscape(v));
                    out.println();
                }
            }
        }
        {
            Set<String> f = mail.toeach();
            if (f != null && f.size() > 0) {
                for (String v : f) {
                    out.print("toeach : ");
                    out.print(lineEscape(v));
                    out.println();
                }
            }
        }
        {
            Set<String> f = mail.cc();
            if (f != null && f.size() > 0) {
                for (String v : f) {
                    out.print("cc : ");
                    out.print(lineEscape(v));
                    out.println();
                }
            }
        }
        {
            Set<String> f = mail.bcc();
            if (f != null && f.size() > 0) {
                for (String v : f) {
                    out.print("bcc : ");
                    out.print(lineEscape(v));
                    out.println();
                }
            }
        }
        {
            Properties f = mail.getProperties();
            if (f != null && f.size() > 0) {
                for (Map.Entry v : f.entrySet()) {
                    out.print("property : ");
                    out.print(lineEscape((String) v.getKey()) + "=" + lineEscape((String) v.getValue()));
                    out.println();
                }
            }
        }
        {
            GoMailDataSource f = mail.repeatDataSource();
            if (f != null) {
                out.print("repeat : ");
                SerializedForm s = f.serialize();
                Set<String> aliases = GoMailDataSourceSerializedFormConfig.getAliasesFor(s.getType());
                String[] aliasesArr = aliases.toArray(new String[aliases.size()]);
                String aliasType = aliasesArr.length == 0 ? s.getType() : aliasesArr[0];
                out.print(lineEscape(aliasType + ":" + s.getValue()));
                out.println();
            }
        }
        {
            for (Map.Entry<String, GoMailDataSource> me : mail.namedDataSources().entrySet()) {
                GoMailDataSource f = me.getValue();
                if (f != null) {
                    out.print("dataSource : ");
                    out.print(me.getKey() + "=");
                    SerializedForm s = f.serialize();
                    Set<String> aliases = GoMailDataSourceSerializedFormConfig.getAliasesFor(s.getType());
                    String[] aliasesArr = aliases.toArray(new String[aliases.size()]);
                    String aliasType = aliasesArr.length == 0 ? s.getType() : aliasesArr[0];
                    out.print(lineEscape(aliasType + ":" + s.getValue()));
                    out.println();
                }
            }
        }
        {
            if (mail.isSimulate()) {
                out.println("simulate : true");
            }
        }
        {
            if (mail.subject() != null) {
                out.println("subject : " + lineEscape(mail.subject()));
            }
        }

        {
            GoMailBodyList f = mail.body();
            if (f != null) {
                for (GoMailBody b : f) {

                    out.print(b.getPosition().toString().toLowerCase() + " : ");
                    if (b.getContentType() != null) {
                        out.print(b.getContentType());
                    }
                    out.print((b.getOrder() != 0 ? ("order=" + b.getOrder()) : ""));
                    if (b.isExpandable()) {
                        out.print(" expandable");
                    } else {
                        out.print(" !expandable");
                    }
                    if (b instanceof GoMailBodyPath) {
                        out.println(" path");
                        out.println(((GoMailBodyPath) b).getPath());
                    } else {
                        GoMailBodyContent c = (GoMailBodyContent) b;
                        if (GoMailUtils.isTextContentType(b.getContentType())) {
                            String s = new String(c.getByteArray());
                            if (s.contains("<<end>>")) {
                                out.println(" base64");
                                s = Base64.getEncoder().encodeToString(c.getByteArray());
                                for (int i = 0; i < s.length(); i += 100) {
                                    int k = Math.min(i + 100, s.length());
                                    out.println(s.substring(i, k));
                                }
                            } else {
                                out.println();
                                out.println(s);
                                out.println("<<end>>");
                            }
                        } else {
                            out.println(" base64");
                            String s = Base64.getEncoder().encodeToString(c.getByteArray());
                            for (int i = 0; i < s.length(); i += 100) {
                                int k = Math.min(i + 100, s.length());
                                out.println(s.substring(i, k));
                            }
                        }
                    }
                }
            }
        }
    }
    private void writeText(GoMailMessage mail, OutputStream stream) throws IOException {
        PrintStream out = (stream instanceof PrintStream) ? ((PrintStream) stream) : new PrintStream(stream);
        out.println(SER_HEADER);
        {
            String f = mail.from();
            if (f != null) {
                out.print("from : ");
                out.print(lineEscape(f));
                out.println();
            }
        }
        {
            Set<String> f = mail.to();
            if (f != null && f.size() > 0) {
                for (String v : f) {
                    out.print("to : ");
                    out.print(lineEscape(v));
                    out.println();
                }
            }
        }
        {
            Set<String> f = mail.cc();
            if (f != null && f.size() > 0) {
                for (String v : f) {
                    out.print("cc : ");
                    out.print(lineEscape(v));
                    out.println();
                }
            }
        }
        {
            Set<String> f = mail.bcc();
            if (f != null && f.size() > 0) {
                for (String v : f) {
                    out.print("bcc : ");
                    out.print(lineEscape(v));
                    out.println();
                }
            }
        }
        {
            Properties f = mail.getProperties();
            if (f != null && f.size() > 0) {
                for (Map.Entry v : f.entrySet()) {
                    out.print("property : ");
                    out.print(lineEscape((String) v.getKey()) + "=" + lineEscape((String) v.getValue()));
                    out.println();
                }
            }
        }
        {
            if (mail.isSimulate()) {
                out.println("simulate : true");
            }
        }
        {
            if (mail.subject() != null) {
                out.println("subject : " + lineEscape(mail.subject()));
            }
        }

        {
            GoMailBodyList f = mail.body();
            if (f != null) {
                for (GoMailBody b : f) {

                    out.print(b.getPosition().toString().toLowerCase() + " : ");
                    if (b.getContentType() != null) {
                        out.print(b.getContentType());
                    }
                    out.print((b.getOrder() != 0 ? ("order=" + b.getOrder()) : ""));
                    if (b.isExpandable()) {
                        out.print(" expandable");
                    } else {
                        out.print(" !expandable");
                    }
                    if (b instanceof GoMailBodyPath) {
                        out.println(" path");
                        out.println(((GoMailBodyPath) b).getPath());
                    } else {
                        GoMailBodyContent c = (GoMailBodyContent) b;
                        if (GoMailUtils.isTextContentType(b.getContentType())) {
                            String s = new String(c.getByteArray());
                            if (s.contains("<<end>>")) {
                                out.println(" base64");
                                s = Base64.getEncoder().encodeToString(c.getByteArray());
                                for (int i = 0; i < s.length(); i += 100) {
                                    int k = Math.min(i + 100, s.length());
                                    out.println(s.substring(i, k));
                                }
                            } else {
                                out.println();
                                out.println(s);
                                out.println("<<end>>");
                            }
                        } else {
                            out.println(" base64");
                            String s = Base64.getEncoder().encodeToString(c.getByteArray());
                            for (int i = 0; i < s.length(); i += 100) {
                                int k = Math.min(i + 100, s.length());
                                out.println(s.substring(i, k));
                            }
                        }
                    }
                }
            }
        }
    }

    private static String lineUnEscape(String v) {
        StringBuilder sb = new StringBuilder();
        boolean wasEscape = false;
        for (char c : v.toCharArray()) {
            switch (c) {
                case '\\': {
                    if (wasEscape) {
                        sb.append('\\');
                        wasEscape = false;
                    } else {
                        wasEscape = true;
                    }
                    break;
                }
                case 'n': {
                    if (wasEscape) {
                        sb.append('\n');
                    } else {
                        sb.append(c);
                    }
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    private String lineEscape(String v) {
        StringBuilder sb = new StringBuilder();
        for (char c : v.toCharArray()) {
            switch (c) {
                case '\\': {
                    sb.append('\\');
                    sb.append(c);
                    break;
                }
                case '\n': {
                    sb.append('\\');
                    sb.append('n');
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public boolean isGoMail(InputStreamSource stream) {
        InputStream in = null;

        try {
            try {
                in = stream.open();
                return isGoMail(in);
            } catch (Exception e) {
                return false;
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Logger.getLogger(GoMailModuleSerializer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public boolean isGoMail(InputStream stream) {
        return getGoMailFormat(stream) != null;
    }

    public GoMailFormat getGoMailFormat(InputStream stream) {
        try {
            byte[] footprint = IOUtils.loadByteArray(stream);
            //trytext
            {
                String s = new String(footprint);
                int i = StringUtils.indexOfRegexpStart(s, "\n|\r");
                String header = (i > 0) ? s.substring(0, i).trim() : s.trim();
                if (header.equals(SER_HEADER)) {
                    return GoMailFormat.TEXT;
                }
            }

            //try object
            {
                //try Object
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(new ByteArrayInputStream(footprint));
                    String header = ois.readUTF();
                    if (SER_HEADER.equals(header)) {
                        return GoMailFormat.OBJECT;
                    }
                } finally {
                    if (ois != null) {
                        ois.close();
                    }
                }
            }
        } catch (IOException ex) {
            //ignore
        }
        return null;
    }

    public static GoMailDataSource deserializeDataSource(String propValue) {
        int dots = propValue.indexOf(':');
        if (dots > 0) {
            String type = lineUnEscape(propValue.substring(0, dots));
            if (StringUtils.isJavaIdentifier(type)) {
                String serVal = lineUnEscape(propValue.substring(dots + 1));
                return deserializeDataSource(type, serVal);
            }
        }
        return deserializeDataSource(null, propValue);
    }

    public static GoMailDataSource deserializeDataSource(String type, String serVal) {
        if (type == null || type.length() == 0) {
            type = "default";
        }
        return new SerializedForm(type, serVal).instantiate(GoMailDataSourceSerializedFormConfig, GoMailDataSource.class);
    }

    public static GoMailDataSourceFilter deserializeDataSourceFilter(String propValue) {
        int dots = propValue.indexOf(':');
        if (dots > 0) {
            String type = lineUnEscape(propValue.substring(0, dots));
            if (StringUtils.isJavaIdentifier(type)) {
                String serVal = lineUnEscape(propValue.substring(dots + 1));
                return deserializeDataSourceFilter(type, serVal);
            }
        }
        return deserializeDataSourceFilter(null, propValue);
    }

    public static GoMailDataSourceFilter deserializeDataSourceFilter(String type, String serVal) {
        if (type == null || type.length() == 0) {
            type = ExprGoMailDataSourceFilter.class.getName();
        }
        return new SerializedForm(type, serVal).instantiate(GoMailDataSourcefilterSerializedFormConfig, GoMailDataSourceFilter.class);
    }
}
