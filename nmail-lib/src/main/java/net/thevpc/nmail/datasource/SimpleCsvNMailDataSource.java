/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail.datasource;

import net.thevpc.nmail.NMailContext;
import net.thevpc.nmail.expr.*;
import net.thevpc.nmail.util.NMailUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author taha.bensalah@gmail.com
 */
public class SimpleCsvNMailDataSource extends AbstractNMailDataSource {

    private List<RowWithId> rows = new ArrayList<>();
    private List<String> columns = new ArrayList<>();
    private Expr arg;

    public SimpleCsvNMailDataSource(Expr arg) {
        this.arg = arg;
    }

    public void build(NMailContext context, ExprVars vars) {
        super.build(context, vars);
        String cwd = (String) vars.get("cwd");
        String path = new ExprEvaluator().evalExpr(arg, String.class, vars);
        if (NMailUtils.isURL(path)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(NMailUtils.toURL(path).openStream()))) {
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
        return rows.get(rowIndex).getValues().get(colIndex);
    }

    @Override
    public String getRowId(int rowIndex) {
        return rows.get(rowIndex).getRowId();
    }

    @Override
    public Expr toExpr() {
        return new FctExpr("csv", new Expr[]{arg});
    }

}
