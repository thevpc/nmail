/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.datasource;

import net.thevpc.gomail.GoMailContext;
import net.thevpc.gomail.expr.*;
import net.thevpc.gomail.util.GoMailUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author taha.bensalah@gmail.com
 */
public class SimpleCsvGoMailDataSource extends AbstractGoMailDataSource {

    private List<List<String>> rows = new ArrayList<>();
    private List<String> columns = new ArrayList<>();
    private Expr[] args;

    public SimpleCsvGoMailDataSource(Expr[] args) {
        if (args.length == 1) {
            this.args = args;
        } else {
            throw new IllegalArgumentException("expected one argument");
        }
    }

    public SimpleCsvGoMailDataSource(Path path) {
        this(new Expr[]{
                new StringExpr(path.toString())
        });
    }

    public void build(GoMailContext context, Map<String, Object> vars) {
        super.build(context, vars);
        String cwd = (String) vars.get("cwd");
        String path = new ExprEvaluator().evalExpr(args[0], String.class, vars);
        if (GoMailUtils.isURL(path)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(GoMailUtils.toURL(path).openStream()))) {
                init(reader);
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        } else {
            Path pp = Paths.get(path);
            if (!pp.isAbsolute() && cwd != null) {
                Path p2 = Paths.get(cwd).resolve(pp);
                if (Files.isRegularFile(p2)) {
                    try (BufferedReader reader = Files.newBufferedReader(p2)) {
                        init(reader);
                    } catch (IOException ex) {
                        throw new IllegalArgumentException(ex);
                    }
                    return;
                }
            }
            if (Files.isRegularFile(pp)) {
                try (BufferedReader reader = Files.newBufferedReader(pp)) {
                    init(reader);
                } catch (IOException ex) {
                    throw new IllegalArgumentException(ex);
                }
                return;
            }
            throw new IllegalArgumentException("csv path not found " + path);
        }
    }

    private void init(BufferedReader reader) {
        try {
            String line;
            line = reader.readLine();
            if (line == null) {
                throw new IllegalArgumentException("missing headers");
            }
            columns = readLine(line);
            if (columns == null || columns.size() == 0) {
                throw new IllegalArgumentException("missing headers");
            }
            while ((line = reader.readLine()) != null) {
                List<String> a = readLine(line);
                if (a != null) {
                    while (a.size() < columns.size()) {
                        a.add("");
                    }
                    rows.add(a);
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private List<String> readLine(String line) {
        if (line == null || line.isEmpty() || line.startsWith("#")) {
            return null;
        }
        StringReader rr = new StringReader(line);
        Token was = null;
        Token t;

        List<String> r = new ArrayList<>();
        while ((t = readColumnOrSep(rr)) != null) {
            if (was == null || was.getTtype() == TokenTType.COMMA) {
                if (t.getTtype() == TokenTType.COMMA) {
                    r.add("");
                } else {
                    r.add(t.getSval());
                }
            } else {
                //last is a string
                if (t.getTtype() == TokenTType.COMMA) {
                    // do nothing
                } else {
                    r.add(t.getSval());
                }
            }
            was = t;
        }
        return r;
    }


    private Token readColumnOrSep(StringReader rr) {
        try {
            int a = rr.read();
            if (a < 0) {
                return null;
            }
            if (a == ',' || a == ';' || a == '\t' || a == '\n' || a == '\r') {
                return new Token(
                        TokenTType.COMMA,
                        String.valueOf((char) a),
                        0,
                        '\0'
                );
            }
            if (a == '\'') {
                StringBuilder sb = new StringBuilder();
                boolean end = false;
                while (!end && (a = rr.read()) > 0) {
                    switch (a) {
                        case '\'': {
                            end = true;
                            break;
                        }
                        case '\\': {
                            a = rr.read();
                            switch (a) {
                                case -1: {
                                    end = true;
                                    break;
                                }
                                case '\'': {
                                    sb.append('\'');
                                    break;
                                }
                                case '\\': {
                                    sb.append('\\');
                                    break;
                                }
                                case 'n': {
                                    sb.append('\n');
                                    break;
                                }
                                case 'r': {
                                    sb.append('\r');
                                    break;
                                }
                                case 'f': {
                                    sb.append('\f');
                                    break;
                                }
                                case 't': {
                                    sb.append('\r');
                                    break;
                                }
                                default: {
                                    sb.append('\\');
                                    sb.append((char) a);
                                }
                            }
                        }
                        default: {
                            sb.append((char) a);
                        }
                    }
                }
                return new Token(
                        TokenTType.SQ_STRING,
                        sb.toString(),
                        0,
                        '\0'
                );
            } else if (a == '\"') {
                StringBuilder sb = new StringBuilder();
                boolean end = false;
                while (!end && (a = rr.read()) > 0) {
                    switch (a) {
                        case '\"': {
                            end = true;
                            break;
                        }
                        case '\\': {
                            a = rr.read();
                            switch (a) {
                                case -1: {
                                    end = true;
                                    break;
                                }
                                case '\'': {
                                    sb.append('\'');
                                    break;
                                }
                                case '\\': {
                                    sb.append('\\');
                                    break;
                                }
                                case 'n': {
                                    sb.append('\n');
                                    break;
                                }
                                case 'r': {
                                    sb.append('\r');
                                    break;
                                }
                                case 'f': {
                                    sb.append('\f');
                                    break;
                                }
                                case 't': {
                                    sb.append('\r');
                                    break;
                                }
                                default: {
                                    sb.append('\\');
                                    sb.append((char) a);
                                }
                            }
                        }
                        default: {
                            sb.append((char) a);
                        }
                    }
                }
                return new Token(
                        TokenTType.DQ_STRING,
                        sb.toString(),
                        0,
                        '\0'
                );
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append((char) a);
                boolean end = false;
                while (!end) {
                    rr.mark(1);
                    a = rr.read();
                    if (a < 0) {
                        break;
                    }
                    switch (a) {
                        case '\t':
                        case ',':
                        case ';': {
                            end = true;
                            rr.reset();
                            break;
                        }
                        case '\\': {
                            a = rr.read();
                            switch (a) {
                                case -1: {
                                    end = true;
                                    break;
                                }
                                case '\'': {
                                    sb.append('\'');
                                    break;
                                }
                                case '\\': {
                                    sb.append('\\');
                                    break;
                                }
                                case 'n': {
                                    sb.append('\n');
                                    break;
                                }
                                case 'r': {
                                    sb.append('\r');
                                    break;
                                }
                                case 'f': {
                                    sb.append('\f');
                                    break;
                                }
                                case 't': {
                                    sb.append('\r');
                                    break;
                                }
                                default: {
                                    sb.append('\\');
                                    sb.append((char) a);
                                }
                            }
                        }
                        default: {
                            sb.append((char) a);
                        }
                    }
                }
                return new Token(
                        TokenTType.WORD,
                        sb.toString().trim(),
                        0,
                        '\0'
                );
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public String[] getColumns() {
        return columns.toArray(new String[0]);
    }

    @Override
    public String getCell(int rowIndex, int colIndex) {
        return rows.get(rowIndex).get(colIndex);
    }

    @Override
    public Expr toExpr() {
        return new FctExpr("csv", new Expr[]{
                new StringExpr(source.toString())
        });
    }

}
