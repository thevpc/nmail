/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.upa;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import net.thevpc.gomail.datasource.AbstractGoMailDataSource;
import net.thevpc.gomail.expr.Expr;
import net.thevpc.gomail.expr.ExprHelper;
import net.thevpc.gomail.util.SerializedForm;
import net.thevpc.upa.bulk.DataColumn;
import net.thevpc.upa.bulk.DataReader;
import net.thevpc.upa.bulk.DataRow;

/**
 * @author taha.bensalah@gmail.com
 */
public abstract class AbstractDataParserGoMailDataSource extends AbstractGoMailDataSource {

    private List<DataRow> rows = new ArrayList<DataRow>();
    private String[] sColumns;
    private boolean parsed = false;

    protected AbstractDataParserGoMailDataSource(Object source) {
        super(source);
    }

    protected abstract DataReader createDataTable() throws IOException;

    private void parse() {
        if (!parsed) {
            parsed = true;
            try {
                DataReader data = createDataTable();

                DataColumn[] columns = data.getColumns();

                String[] colString = new String[columns.length];
                for (int i = 0; i < colString.length; i++) {
                    colString[i] = columns[i].getName();

                }
                sColumns = colString;

                while (data.hasNext()) {
                    rows.add(data.readRow());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getColumnCount() {
        parse();
        return sColumns.length;
    }

    @Override
    public int getRowCount() {
        parse();
        return rows.size();
    }

    @Override
    public String[] getColumns() {
        parse();
        return sColumns;
    }

    @Override
    public String getCell(int rowIndex, int colIndex) {
        parse();
        Object cell = rows.get(rowIndex).getValues()[colIndex];
        return formatString(cell);
    }

    private String formatString(Object val) {
        return val == null ? null : val.toString();
    }

}
