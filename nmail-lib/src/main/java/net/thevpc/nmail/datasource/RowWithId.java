package net.thevpc.nmail.datasource;

import java.util.List;

public class RowWithId {
    private final List<String> values;
    private final long rowIndex;

    public RowWithId(long rowIndex, List<String> values) {
        this.rowIndex = rowIndex;
        this.values = values;
    }

    public long getRowIndex() {
        return rowIndex;
    }


    public List<String> getValues() {
        return values;
    }

    public String getRowId() {
        return String.valueOf(rowIndex+1);
    }
}
