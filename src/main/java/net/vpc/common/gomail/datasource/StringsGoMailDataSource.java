/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail.datasource;

import net.vpc.common.gomail.util.SerializedForm;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class StringsGoMailDataSource extends AbstractGoMailDataSource {

    private String[] columns;

    public StringsGoMailDataSource(String[][] values, String[] columns) {
        super(values);
        this.columns = columns;
    }

    @Override
    public SerializedForm serialize() {
        throw new IllegalArgumentException("Not yet supported");
    }

    @Override
    public String getCell(int rowIndex, int colIndex) {
        String[][] values = (String[][]) getBuildSource();
        return values[rowIndex][colIndex];
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public int getRowCount() {
        String[][] values = (String[][]) getBuildSource();
        return values.length;
    }

    @Override
    public String[] getColumns() {
        return columns;
    }

}
