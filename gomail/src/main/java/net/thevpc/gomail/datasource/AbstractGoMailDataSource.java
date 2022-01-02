/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.datasource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import net.thevpc.gomail.GoMailContext;
import net.thevpc.gomail.GoMailDataSource;
import net.thevpc.gomail.GoMailDataSourceRow;
import net.thevpc.gomail.util.GoMailUtils;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public abstract class AbstractGoMailDataSource implements GoMailDataSource {

    private GoMailDataSourceRow[] rows;
    private Map<String, Integer> indexes;
    private GoMailContext context;

    protected AbstractGoMailDataSource() {
    }

    @Override
    public void build(GoMailContext context, Map<String, Object> vars) {
        this.context = context;
    }

    public GoMailContext getContext() {
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
    public GoMailDataSourceRow getRow(int rowIndex) {
        return getRows()[rowIndex];
    }

//    @Override
    public GoMailDataSourceRow[] getRows() {
        if (rows == null) {
            rows = new GoMailDataSourceRow[getRowCount()];
            for (int i = 0; i < rows.length; i++) {
                rows[i] = new IndexedRow(i);
            }
        }
        return rows;
    }

    private class IndexedRow implements GoMailDataSourceRow {

        private int rowIndex;

        public IndexedRow(int index) {
            this.rowIndex = index;
        }

        @Override
        public String get(int index) {
            return getCell(rowIndex, index);
        }

        @Override
        public String[] getColumns() {
            return GoMailUtils.getColumns(AbstractGoMailDataSource.this);
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

    }

}
