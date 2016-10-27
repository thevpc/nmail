/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail.datasource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import net.vpc.common.gomail.util.SerializedForm;
import net.vpc.upa.bulk.DataColumn;
import net.vpc.upa.bulk.DataReader;
import net.vpc.upa.bulk.DataRow;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public abstract class AbstractDataParserGoMailDataSource extends AbstractGoMailDataSource {

    private List<DataRow> rows = new ArrayList<DataRow>();
    private String[] sColumns;
    private boolean parsed = false;

    protected AbstractDataParserGoMailDataSource(Object source) {
        super(source);
    }

    @Override
    public SerializedForm serialize() {
        String u = null;
        Object source = getSource();
        if (source instanceof File) {
            try {
                u = ((File) source).toURI().toURL().toString();
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        } else if (source instanceof URL) {
            u = ((URL) source).toString();
        } else if (source instanceof String) {
            u = ((String) source);
        } else if (source instanceof byte[]) {
            u = Base64.getEncoder().encodeToString(((byte[]) source));
        } else {
            throw new IllegalArgumentException("Unable to serialize " + source);
        }
        return new SerializedForm(getClass().getName(), u);
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
    public String getCell(int rowIndex, int colIndex) {
        parse();
        Object cell = rows.get(rowIndex).getValues()[colIndex];
        return formatString(cell);
    }

    @Override
    public int getColumnCount() {
        parse();
        return sColumns.length;
    }

    @Override
    public String[] getColumns() {
        parse();
        return sColumns;
    }

    private String formatString(Object val) {
        return val == null ? null : val.toString();
    }

    @Override
    public int getRowCount() {
        parse();
        return rows.size();
    }

}
