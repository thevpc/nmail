/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail;

import net.thevpc.nmail.expr.Expr;
import net.thevpc.nmail.expr.ExprVars;

/**
 * @author taha.bensalah@gmail.com
 */
public interface NMailDataSource {

    void build(NMailContext context, ExprVars vars);

    int getColumnCount();

    int getRowCount();

    String getColumn(int index);

    NMailDataSourceRow getRow(int rowIndex);

    String getCell(int rowIndex, int colIndex);

    String getRowId(int rowIndex);

    String getCell(int rowIndex, String colName);

    Expr toExpr();
}
