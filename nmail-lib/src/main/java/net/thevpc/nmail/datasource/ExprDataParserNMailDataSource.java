/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail.datasource;

import net.thevpc.nmail.NMailContext;
import net.thevpc.nmail.NMailDataSource;
import net.thevpc.nmail.expr.Expr;
import net.thevpc.nmail.expr.ExprVars;

/**
 * @author taha.bensalah@gmail.com
 */
public class ExprDataParserNMailDataSource extends AbstractNMailDataSource {

    private NMailDataSource base;
    private Expr baseExpr;

    public ExprDataParserNMailDataSource(Expr baseExpr) {
        this.baseExpr = baseExpr;
    }

    @Override
    public void build(NMailContext context, ExprVars vars) {
        super.build(context, vars);
        base = context.buildDataSource(baseExpr, vars);
    }

    @Override
    public int getColumnCount() {
        return base.getColumnCount();
    }

    @Override
    public int getRowCount() {
        return base.getRowCount();
    }

    @Override
    public String getColumn(int index) {
        return base.getColumn(index);
    }

    @Override
    public String getCell(int rowIndex, int colIndex) {
        return base.getCell(rowIndex,colIndex);
    }


    public Expr toExpr() {
        return base.toExpr();
    }

    @Override
    public String getRowId(int rowIndex) {
        return base.getRowId(rowIndex);
    }
}
