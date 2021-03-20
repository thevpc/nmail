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

/**
 *
 * @author taha.bensalah@gmail.com
 */
public abstract class AbstractGoMailDataSource implements GoMailDataSource {

    private GoMailDataSourceRow[] rows;
    private Object source;
    private Object buildSource;
    private Map<String, Integer> indexes;
    private GoMailContext context;

    protected AbstractGoMailDataSource(Object source) {
        this.source = source;
    }

    @Override
    public void build(GoMailContext context) {
        if (source instanceof String) {
            buildSource = context.eval((String) source);
        } else if (source instanceof File) {
            buildSource = new File(context.eval(((File) source).getPath()));
        } else if (source instanceof URL) {
            try {
                buildSource = new URL(context.eval(((File) source).getPath()));
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            buildSource = source;
        }
        this.context = context;
    }

    public GoMailContext getContext() {
        return context;
    }

    protected Object getBuildSource() {
        if (buildSource == null) {
            throw new IllegalArgumentException("Unresolved source");
        }
        return buildSource;
    }

    protected Object getSource() {
        return source;
    }

    protected int indexOf(String colName) {
        if (indexes == null) {
            indexes = new HashMap<>();
            for (int i = 0; i < getColumnCount(); i++) {
                indexes.put(getColumns()[i], i);
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.source);
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
        final AbstractGoMailDataSource other = (AbstractGoMailDataSource) obj;
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        return true;
    }

}
