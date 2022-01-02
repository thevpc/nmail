/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.datasource;

import net.thevpc.gomail.GoMailContext;
import net.thevpc.gomail.GoMailDataSource;
import net.thevpc.gomail.GoMailDataSourceRow;
import net.thevpc.gomail.expr.Expr;
import net.thevpc.gomail.expr.OpExpr;
import net.thevpc.gomail.expr.TokenTType;
import net.thevpc.gomail.util.MyGoMailDataSourceFilter;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author taha.bensalah@gmail.com
 */
public class ExprDataParserGoMailDataSource extends AbstractGoMailDataSource {

    private GoMailDataSource base;
    private Expr baseExpr;

    public ExprDataParserGoMailDataSource(Expr baseExpr) {
        this.baseExpr = baseExpr;
    }

    @Override
    public void build(GoMailContext context, Map<String, Object> vars) {
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
    public String[] getColumns() {
        return base.getColumns();
    }

    @Override
    public String getCell(int rowIndex, int colIndex) {
        return base.getCell(rowIndex,colIndex);
    }


    public Expr toExpr() {
        return base.toExpr();
    }
}
