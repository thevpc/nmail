/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail;

import net.vpc.common.gomail.util.SerializedForm;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface GoMailDataSource {

    void build(GoMailContext context);

    int getColumnCount();

    int getRowCount();

    String[] getColumns();

    GoMailDataSourceRow getRow(int rowIndex);

    String getCell(int rowIndex, int colIndex);

    String getCell(int rowIndex, String colName);

    public SerializedForm serialize();
}
