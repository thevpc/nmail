package net.thevpc.gomail;

/**
 * Created by vpc on 10/26/16.
 */
public interface GoMailDataSourceRow {

    String get(int index);

    String get(String name);

    String[] getColumns();
}
