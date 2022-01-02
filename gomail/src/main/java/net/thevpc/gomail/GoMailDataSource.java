/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail;

import net.thevpc.gomail.expr.Expr;

import java.util.Map;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface GoMailDataSource {

    void build(GoMailContext context, Map<String, Object> vars);

    int getColumnCount();

    int getRowCount();

    String getColumn(int index);

    GoMailDataSourceRow getRow(int rowIndex);

    String getCell(int rowIndex, int colIndex);

    String getCell(int rowIndex, String colName);

    Expr toExpr();
}
