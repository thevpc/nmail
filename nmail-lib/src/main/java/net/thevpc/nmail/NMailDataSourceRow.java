package net.thevpc.nmail;

/**
 * Created by vpc on 10/26/16.
 */
public interface NMailDataSourceRow {

    String rowId();

    String get(int index);

    String get(String name);

    String[] getColumns();
}
