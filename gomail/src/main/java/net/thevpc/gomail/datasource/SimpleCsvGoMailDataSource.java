/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.datasource;

import net.thevpc.gomail.GoMailContext;
import net.thevpc.gomail.expr.*;
import net.thevpc.gomail.util.CsvParser;
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
        CsvParser p=new CsvParser(reader);
        columns=p.getColumns();
        rows=p.getRows();
    }

    @Override
    public String getColumn(int index) {
        return columns.get(index);
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
    public String getCell(int rowIndex, int colIndex) {
        return rows.get(rowIndex).get(colIndex);
    }

    @Override
    public Expr toExpr() {
        return new FctExpr("csv", args);
    }

}
