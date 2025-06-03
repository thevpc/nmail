/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail.datasource;

import net.thevpc.nmail.NMailContext;
import net.thevpc.nmail.expr.Expr;
import net.thevpc.nmail.expr.ExprEvaluator;
import net.thevpc.nmail.expr.ExprVars;
import net.thevpc.nmail.expr.StringExpr;
import net.thevpc.nmail.util.NMailUtils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author taha.bensalah@gmail.com
 */
public class StringsNMailDataSource extends AbstractNMailDataSource {

    private List<String> columns;
    private List<RowWithId> rows;
    private Expr arg;

    public StringsNMailDataSource(String[][] values, String[] columns) {
        this.rows = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            String[] value = values[i];
            rows.add(new RowWithId(i, new ArrayList<>(Arrays.asList(value))));
        }
        this.columns = new ArrayList<>(Arrays.asList(columns));
    }

    public StringsNMailDataSource(Expr arg) {
        this.arg = arg;
    }

    @Override
    public void build(NMailContext context, ExprVars vars) {
        super.build(context, vars);
        if (arg != null) {
            CsvParser p = new CsvParser(new StringReader(new ExprEvaluator().evalExpr(arg, String.class, vars)));
            columns = p.getColumns();
            rows = p.getRows();
        }
    }

    public String csv() {
        StringBuilder sb = new StringBuilder("");

        for (String column : columns) {
            sb.append(NMailUtils.escapeStringWithDoubleQuotes(column));
            sb.append(",");
        }
        sb.append("\n");

        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i).getValues();
            if (i > 0) {
                sb.append(',');
            }
            for (int j = 0; j < row.size(); j++) {
                if (j > 0) {
                    sb.append(',');
                }
                sb.append(NMailUtils.escapeStringWithDoubleQuotes(row.get(j)));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public String json() {
        StringBuilder sb = new StringBuilder("{");

        sb.append('[');
        for (String column : columns) {
            sb.append(NMailUtils.escapeStringWithSimpleQuotes(column));
        }
        sb.append(']');

        sb.append('[');
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i).getValues();
            if (i > 0) {
                sb.append(',');
            }
            sb.append('[');
            for (int j = 0; j < row.size(); j++) {
                if (j > 0) {
                    sb.append(',');
                }
                sb.append(NMailUtils.escapeStringWithSimpleQuotes(row.get(j)));
            }
            sb.append(']');
        }
        sb.append(']');

        sb.append('}');
        return sb.toString();
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
    public String getColumn(int index) {
        return columns.get(index);
    }

    @Override
    public String getCell(int rowIndex, int colIndex) {
        return rows.get(rowIndex).getValues().get(colIndex);
    }

    public Expr toExpr() {
        return new StringExpr(csv());
    }

    @Override
    public String getRowId(int rowIndex) {
        return rows.get(rowIndex).getRowId();
    }
}
