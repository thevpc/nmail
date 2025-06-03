/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail.datasource;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.thevpc.nmail.NMailContext;
import net.thevpc.nmail.NMailDataSource;
import net.thevpc.nmail.NMailDataSourceRow;
import net.thevpc.nmail.expr.Expr;
import net.thevpc.nmail.expr.ExprVars;
import net.thevpc.nmail.expr.OpExpr;
import net.thevpc.nmail.expr.TokenTType;
import net.thevpc.nmail.util.MyNMailDataSourceFilter;

/**
 * @author taha.bensalah@gmail.com
 */
public class FilteredDataParserNMailDataSource extends AbstractNMailDataSource {

    private List<NMailDataSourceRow> rows = new ArrayList<NMailDataSourceRow>();
    private String[] sColumns;
    private boolean parsed = false;
    private NMailDataSourceFilter filter;
    private NMailDataSource base;
    private Expr filterExpr;
    private Expr baseExpr;

    public FilteredDataParserNMailDataSource(Expr baseExpr, Expr filterExpr) {
        this.baseExpr = baseExpr;
        this.filterExpr = filterExpr;
    }

    @Override
    public void build(NMailContext context, ExprVars vars) {
        super.build(context, vars);
        base = context.buildDataSource(baseExpr,vars);
        filter = filterExpr == null ? null : new MyNMailDataSourceFilter(filterExpr,vars);
    }

    private void parse() {
        if (!parsed) {
            parsed = true;
            NMailDataSource data = base;
            String[] colString = new String[data.getColumnCount()];
            for (int i = 0; i < colString.length; i++) {
                colString[i] = data.getColumn(i);
            }
            sColumns = colString;
            int max = data.getRowCount();
            int ignoreCount=0;
            for (int i = 0; i < max; i++) {
                NMailDataSourceRow r = data.getRow(i);
                if (filter.accept(getContext(), r)) {
                    rows.add(r);
                }else{
                    filter.accept(getContext(), r);
                    ignoreCount++;
                    Logger.getLogger(FilteredDataParserNMailDataSource.class.getName()).log(Level.INFO,"["+baseExpr+"|"+filterExpr+"] Filtered row (ignored) "+r);
                }
            }
            Logger.getLogger(FilteredDataParserNMailDataSource.class.getName()).log(Level.INFO,"["+baseExpr+"|"+filterExpr+"] Total Filtered "+ignoreCount+"/"+max);
        }
    }

    @Override
    public int getColumnCount() {
        parse();
        return sColumns.length;
    }

    @Override
    public int getRowCount() {
        parse();
        return rows.size();
    }

    @Override
    public String getColumn(int index) {
        parse();
        return sColumns[index];
    }

    @Override
    public String getCell(int rowIndex, int colIndex) {
        parse();
        return rows.get(rowIndex).get(colIndex);
    }

    @Override
    public String getRowId(int rowIndex) {
        parse();
        return rows.get(rowIndex).rowId();
    }

    public Expr toExpr() {
        return new OpExpr(TokenTType.PIPE, baseExpr, filterExpr);
    }

    public static class GSelect {

        private List<String> columns = new ArrayList<>();
        private String source;
        private String alias;
        private StreamTokenizer tok;
        private boolean peeked;
        private int curr;
        private String sval;

        public GSelect(String str) {
            tok = new StreamTokenizer(new StringReader(str));
            tok.resetSyntax();
            tok.wordChars('a', 'z');
            tok.wordChars('A', 'Z');
            tok.wordChars('.', '.');
            tok.wordChars(128 + 32, 255);
            tok.whitespaceChars(0, ' ');
            tok.quoteChar('"');
            tok.quoteChar('\'');
            parse();
        }

        private boolean isPeekString() {
            peek();
            if (curr == StreamTokenizer.TT_WORD) {
                return true;
            }
            if (curr == '\"') {
                return true;
            }
            if (curr == '\'') {
                return true;
            }
            return false;
        }

        private String nextRequiredString() {
            peek();
            String ret = null;
            switch (curr) {
                case StreamTokenizer.TT_WORD:
                    ret = sval;
                    break;
                case '\"':
                    ret = sval;
                    break;
                case '\'':
                    ret = sval;
                    break;
                default:
                    throw new IllegalArgumentException("expected string");
            }
            next();
            return ret;
        }

        private boolean next() {
            if (peeked) {
                peeked = false;
                peek();
            }
            return curr != StreamTokenizer.TT_EOF;
        }

        private boolean peek() {
            if (!peeked) {
                try {
                    curr = tok.nextToken();
                } catch (IOException ex) {
                    throw new IllegalArgumentException(ex);
                }
                peeked = true;
                sval = tok.sval;
            }
            return curr != StreamTokenizer.TT_EOF;
        }

        private boolean isEOF() {
            return !peek();
        }

        private boolean isPeekFrom() {
            peek();
            if (curr == StreamTokenizer.TT_WORD) {
                if (sval.equals("from")) {
                    return true;
                }
            }
            return false;
        }

        private boolean isPeekComma() {
            peek();
            if (curr == ',') {
                return true;
            }
            return false;
        }

        private void parse() {
            while (isEOF()) {
                if (isPeekFrom()) {
                    next();//skip from
                    source = nextRequiredString();
                    if (isPeekString()) {
                        alias = nextRequiredString();
                    }
                } else if (isPeekComma()) {
                    next(); //skip
                } else {
                    columns.add(nextRequiredString());
                }
            }
        }

        public List<String> getColumns() {
            return columns;
        }

        public String getSource() {
            return source;
        }

        public String getAlias() {
            return alias;
        }

    }

}
