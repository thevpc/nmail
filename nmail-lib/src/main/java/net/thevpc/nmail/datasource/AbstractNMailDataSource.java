/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail.datasource;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import net.thevpc.nmail.NMailContext;
import net.thevpc.nmail.NMailDataSource;
import net.thevpc.nmail.NMailDataSourceRow;
import net.thevpc.nmail.expr.ExprVars;
import net.thevpc.nmail.util.NMailUtils;

/**
 * @author taha.bensalah@gmail.com
 */
public abstract class AbstractNMailDataSource implements NMailDataSource {

    private NMailDataSourceRow[] rows;
    private Map<String, Integer> indexes;
    private NMailContext context;

    protected AbstractNMailDataSource() {
    }

    @Override
    public void build(NMailContext context, ExprVars vars) {
        this.context = context;
    }

    public NMailContext getContext() {
        return context;
    }

    protected int indexOf(String colName) {
        if (indexes == null) {
            indexes = new HashMap<>();
            for (int i = 0; i < getColumnCount(); i++) {
                indexes.put(getColumn(i), i);
            }
        }
        Integer ii = indexes.get(colName);
        if (ii == null) {
            throw new NoSuchElementException(colName);
        }
        return ii;
    }

    @Override
    public String getCell(int rowIndex, String colName) {
        return getCell(rowIndex, indexOf(colName));
    }

    @Override
    public NMailDataSourceRow getRow(int rowIndex) {
        return getRows()[rowIndex];
    }

    //    @Override
    public NMailDataSourceRow[] getRows() {
        if (rows == null) {
            rows = new NMailDataSourceRow[getRowCount()];
            for (int i = 0; i < rows.length; i++) {
                rows[i] = new IndexedRow(i, getRowId(i));
            }
        }
        return rows;
    }

    private class IndexedRow implements NMailDataSourceRow {

        private int rowIndex;
        private String rowId;

        public IndexedRow(int index, String rowId) {
            this.rowIndex = index;
            this.rowId = rowId;
        }

        @Override
        public String rowId() {
            return rowId;//String.valueOf(rowIndex+1);
        }

        @Override
        public String get(int index) {
            return getCell(rowIndex, index);
        }

        @Override
        public String[] getColumns() {
            return NMailUtils.getColumns(AbstractNMailDataSource.this);
        }

        @Override
        public String get(String name) {
            try {
                return getCell(rowIndex, name);
            } catch (NoSuchElementException e) {
                return null;
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 31 * hash + this.rowIndex;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IndexedRow other = (IndexedRow) obj;
            if (this.rowIndex != other.rowIndex) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Row[" + (rowIndex + 1) + "]{");
            for (int i = 0; i < getColumnCount(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(getColumn(i)).append("=").append(get(i));
            }
            sb.append("}");
            return sb.toString();
        }
    }

}
