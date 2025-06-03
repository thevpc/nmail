/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail.modules;

import net.thevpc.nmail.*;
import net.thevpc.nmail.datasource.factories.ServiceNMailDataSourceFactory;
import net.thevpc.nmail.expr.AssignExpr;
import net.thevpc.nmail.expr.Expr;
import net.thevpc.nmail.expr.ExprHelper;
import net.thevpc.nmail.expr.ExprParser;
import net.thevpc.nmail.util.NMailUtils;
import net.thevpc.nmail.util.SerializedForm;
import net.thevpc.nmail.util.SerializedFormConfig;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.*;

import net.thevpc.nmail.datasource.NMailDataSourceFilter;

/**
 * @author taha.bensalah@gmail.com
 */
public class NMailModuleSerializerAsText {

    public static final String SER_HEADER = "#mimetype=application/x-nmail";

    public static final SerializedFormConfig nMailDataSourceSerializedFormConfig
            = new SerializedFormConfig()
            .addImport(NMailDataSource.class.getPackage().getName());

    private static String lineUnEscape(String v) {
        if(v!=null) {
            String vv=v.trim();
            if(vv.startsWith("\"") && vv.endsWith("\"") && vv.length()>1){
                vv=vv.substring(1,vv.length()-1);
                return vv;
            }
        }
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

    public static Expr[] deserializeDataSourceArgs(String propValue, BufferedReader reader) {
        List<Expr> r = new ArrayList<>(
                Arrays.asList(
                        ExprHelper.toStatements(new ExprParser(propValue).parseStatementList())
                )
        );
        if (r.size() > 0) {
            Expr last = r.get(r.size() - 1);
            if (last.toWordExpr() != null && "readlines".equals(last.toWordExpr().getName())) {
                try {
                    r.remove(r.size() - 1);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    boolean first = true;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty()) {
                            break;
                        }
                        if (first) {
                            first = false;
                        } else {
                            sb.append("\n");
                        }
                        sb.append(line);
                    }
                    r.add(ExprHelper.assign("readlines", sb.toString()));
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        }
        return r.toArray(new Expr[0]);
    }

    public static NMailDataSource deserializeDataSource(String propValue, BufferedReader reader) {
        return deserializeDataSource(deserializeDataSourceArgs(propValue, reader));
    }

    public static NMailDataSource deserializeDataSource(Expr[] r) {
        return new SerializedForm(r).instantiate(nMailDataSourceSerializedFormConfig, NMailDataSource.class);
    }

    public static NMailDataSourceFilter deserializeDataSourceFilter(Expr[] r) {
        List<Expr> all = new ArrayList<>(Arrays.asList(r));
        Expr ezz = ExprHelper.searchValueByKey("type", r);
        if (ezz == null) {
            Expr e = ExprHelper.assign("type", "expr");
            all.add(0, e);
        }
        return new SerializedForm(all.toArray(new Expr[0])).instantiate(nMailDataSourceSerializedFormConfig, NMailDataSourceFilter.class);
    }

    public void write(NMail mail, NMailFormat format, File file) throws IOException {
        write(mail, format, file);
    }

    public void write(NMailMessage mail, NMailFormat format, File file) throws IOException {
        write(mail, format, file);
    }

    public String nToString(NMail mail) {
        try {
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            write(mail, NMailFormat.TEXT, s);
            return new String(s.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String nToString(NMailMessage mail) {
        try {
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            write(mail, NMailFormat.TEXT, s);
            return new String(s.toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }



    public void write(NMail mail, NMailFormat format, OutputStream stream) throws IOException {
        switch (format) {
            case TEXT: {
                writeText(mail, stream);
                return;
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }

    public void write(NMailMessage mail, NMailFormat format, OutputStream stream) throws IOException {
        switch (format) {
            case TEXT: {
                writeText(mail, stream);
                return;
            }
        }
        throw new IllegalArgumentException("Unsupported");
    }



    public NMail read(Reader stream) {
        return readText(new BufferedReader(stream));
    }

    private NMail readText(BufferedReader reader) {
        try {
            String line = reader.readLine();
            if (!SER_HEADER.equals((line == null ? "" : line).trim())) {
                throw new IllegalArgumentException("Expected " + SER_HEADER);
            }
            NMail m = new NMail();
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
                            int i = NMailUtils.indexOfRegexpEnd(line, "(:| )+");
                            String propValue = line.substring(i).trim();
                            if (cmd.equals("from")) {
                                m.from(lineUnEscape(propValue));
                            } else if (cmd.equals("provider")) {
                                m.provider(lineUnEscape(propValue));
                            } else if (cmd.equals("tracker")) {
                                m.tracker(new NTrackerFile(lineUnEscape(propValue)));
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
                            } else if (cmd.equalsIgnoreCase("user") || cmd.equalsIgnoreCase("username")) {
                                m.getProperties().put("app.mail.user", propValue);
                            } else if (cmd.equals("password")) {
                                m.getProperties().put("app.mail.password", propValue);
                            } else if (cmd.equals("ask-password")|| cmd.equalsIgnoreCase("askpassword")) {
                                m.getProperties().put("app.mail.ask-password", propValue);
                            } else if (cmd.equals("property")) {
                                int eq = propValue.indexOf('=');
                                m.getProperties().put(
                                        lineUnEscape(propValue.substring(0, eq)), lineUnEscape(propValue.substring(eq + 1))
                                );
                            } else if (cmd.equals("repeat")) {
                                Expr baseExpr = new ExprParser(propValue).parseStatementList();
                                m.repeatDatasource(ServiceNMailDataSourceFactory.getInstance().create(baseExpr));
                            } else if (cmd.equals("datasource")) {
                                Expr[] r = deserializeDataSourceArgs(propValue, reader);

                                if(r.length==0){
                                    throw new IllegalArgumentException("missing datasource args");
                                }
                                if(r.length>1){
                                    throw new IllegalArgumentException("too many datasource args");
                                }
                                String name ="";
                                if(r[0].isAssign()){
                                    AssignExpr a = r[0].toAssign();
                                    r[0]= a.getValue();
                                    name=a.getKey().asString();
                                }
                                if (name == null) {
                                    name = "";
                                }
                                if (m.namedDataSources().containsKey(name)) {
                                    //this is a filtered ds
                                    throw new IllegalArgumentException("Datasource is already defined '" + name + "'");
                                }
                                NMailDataSource d = ServiceNMailDataSourceFactory.getInstance().create(r[0]);
                                m.namedDataSources().put(name, d);
                            } else if (cmd.equals("dry")) {
                                m.setDry(Boolean.valueOf(propValue));
                            } else if (cmd.equals("object") || cmd.equals("attachment") || cmd.equals("footer") || cmd.equals("header")) {
                                NMailBodyPosition pos = NMailBodyPosition.valueOf(cmd.toUpperCase());
                                boolean expandable = true;
                                String contentType = NMail.HTML_CONTENT_TYPE;
                                String charSet = "charset=UTF-8";
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
                                        } else if (pp.startsWith("charset=")) {
                                            charSet = pp;
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
                                        m.body(new URL(pathValue), contentType + ";" + charSet);
                                        NMailBody last = m.body().get(m.body().size() - 1);
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
                                    NMailBody last = m.body().get(m.body().size() - 1);
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
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void writeText(NMail mail, OutputStream stream) throws IOException {
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
            NMailDataSource f = mail.repeatDataSource();
            if (f != null) {
                out.println("repeat : " + f.toExpr());
            }
        }
        {
            for (Map.Entry<String, NMailDataSource> me : mail.namedDataSources().entrySet()) {
                NMailDataSource f = me.getValue();
                if (f != null) {
                    out.print("dataSource : ");
                    out.println(me.getKey() + "=" + f.toExpr());
                }
            }
        }
        {
            if (mail.isDry()) {
                out.println("dry : true");
            }
        }
        {
            if (mail.subject() != null) {
                out.println("subject : " + lineEscape(mail.subject()));
            }
        }

        {
            NMailBodyList f = mail.body();
            if (f != null) {
                for (NMailBody b : f) {

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
                    if (b instanceof NMailBodyPath) {
                        out.println(" path");
                        out.println(((NMailBodyPath) b).getPath());
                    } else {
                        NMailBodyContent c = (NMailBodyContent) b;
                        if (NMailUtils.isTextContentType(b.getContentType())) {
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

    private void writeText(NMailMessage mail, OutputStream stream) throws IOException {
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
            if (f != null && !f.isEmpty()) {
                for (Map.Entry v : f.entrySet()) {
                    out.print("property : ");
                    out.print(lineEscape((String) v.getKey()) + "=" + lineEscape((String) v.getValue()));
                    out.println();
                }
            }
        }
        {
            if (mail.isDry()) {
                out.println("dry : true");
            }
        }
        {
            if (mail.subject() != null) {
                out.println("subject : " + lineEscape(mail.subject()));
            }
        }

        {
            NMailBodyList f = mail.body();
            if (f != null) {
                for (NMailBody b : f) {

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
                    if (b instanceof NMailBodyPath) {
                        out.println(" path");
                        out.println(((NMailBodyPath) b).getPath());
                    } else {
                        NMailBodyContent c = (NMailBodyContent) b;
                        if (NMailUtils.isTextContentType(b.getContentType())) {
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


}
