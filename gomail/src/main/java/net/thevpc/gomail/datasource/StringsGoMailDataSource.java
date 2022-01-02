/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.datasource;

import net.thevpc.gomail.GoMailContext;
import net.thevpc.gomail.expr.Expr;
import net.thevpc.gomail.expr.ExprEvaluator;
import net.thevpc.gomail.expr.StringExpr;
import net.thevpc.gomail.util.CsvParser;
import net.thevpc.gomail.util.GoMailUtils;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author taha.bensalah@gmail.com
 */
public class StringsGoMailDataSource extends AbstractGoMailDataSource {

    private List<String> columns;
    private List<List<String>> rows;
    private Expr[] args;

    public StringsGoMailDataSource(String[][] values, String[] columns) {
        this.rows = new ArrayList<>();
        for (String[] value : values) {
            rows.add(new ArrayList<>(Arrays.asList(value)));
        }
        this.columns = new ArrayList<>(Arrays.asList(columns));
    }

    public StringsGoMailDataSource(Expr[] args) {
        if (args.length == 1) {
            this.args = args;
        } else {
            throw new IllegalArgumentException("unsupported");
        }
    }

    @Override
    public void build(GoMailContext context, Map<String, Object> vars) {
        super.build(context, vars);
        if (args != null) {
            CsvParser p = new CsvParser(new StringReader(new ExprEvaluator().evalExpr(args[0], String.class, vars)));
            columns = p.getColumns();
            rows = p.getRows();
        }
    }

    public String csv() {
        StringBuilder sb = new StringBuilder("");

        for (String column : columns) {
            sb.append(GoMailUtils.escapeStringWithDoubleQuotes(column));
            sb.append(",");
        }
        sb.append("\n");

        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (i > 0) {
                sb.append(',');
            }
            for (int j = 0; j < row.size(); j++) {
                if (j > 0) {
                    sb.append(',');
                }
                sb.append(GoMailUtils.escapeStringWithDoubleQuotes(row.get(j)));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public String json() {
        StringBuilder sb = new StringBuilder("{");

        sb.append('[');
        for (String column : columns) {
            sb.append(GoMailUtils.escapeStringWithSimpleQuotes(column));
        }
        sb.append(']');

        sb.append('[');
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append('[');
            for (int j = 0; j < row.size(); j++) {
                if (j > 0) {
                    sb.append(',');
                }
                sb.append(GoMailUtils.escapeStringWithSimpleQuotes(row.get(j)));
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
        return rows.get(rowIndex).get(colIndex);
    }

    public Expr toExpr() {
        return new StringExpr(csv());
    }


}
