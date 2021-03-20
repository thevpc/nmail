/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.datasource;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.gomail.GoMailContext;
import net.thevpc.gomail.GoMailDataSource;
import net.thevpc.gomail.GoMailDataSourceRow;
import net.thevpc.gomail.modules.GoMailModuleSerializer;
import net.thevpc.gomail.util.ExprList;
import net.thevpc.gomail.util.SerializedForm;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class FilteredDataParserGoMailDataSource extends AbstractGoMailDataSource {

    private List<GoMailDataSourceRow> rows = new ArrayList<GoMailDataSourceRow>();
    private String[] sColumns;
    private boolean parsed = false;
    private GoMailDataSourceFilter filter;
    private GoMailDataSource dataSource;
//    private Map<String, XMailDataSource> dataSources;

    public FilteredDataParserGoMailDataSource(String source) {
        super(source);
//        this.filter = filter;
//        this.dataSources = dataSources;
    }

    @Override
    public SerializedForm serialize() {
//        XMailDataSource ss = (XMailDataSource) getSource();
        //"'" + ss.serialize() + "' where " + filter.serialize()
        return new SerializedForm(
                new ExprList().addAll(
                        ExprList.createKeyValue("type", getClass().getName()),
                        ExprList.createKeyValue("value", getSource().toString())
                )
        );
    }

    public static FilteredDataParserGoMailDataSource valueOf(SerializedForm s) {
        return new FilteredDataParserGoMailDataSource(s.getValue());

//        return new FilteredDataParserXMailDataSource(new HashMap<String, XMailDataSource>(), ds, dsf);
    }

    @Override
    public void build(GoMailContext context) {
        super.build(context);

        String ss = (String) getBuildSource();
        GSelect s = new GSelect(ss);
        String en = s.getSource();
        String ea = s.getAlias();
        dataSource = null;
        if (en == null && ea != null) {
            dataSource = context.getRegisteredDataSources().get(ea);
            if (dataSource == null) {
                dataSource = GoMailModuleSerializer.deserializeDataSource(ea, null);
            }
        } else if (en != null && ea == null) {
            dataSource = context.getRegisteredDataSources().get(en);
            if (dataSource == null) {
                dataSource = GoMailModuleSerializer.deserializeDataSource(en, null);
            }
        } else {
            dataSource = GoMailModuleSerializer.deserializeDataSource(en, /*ea,*/ null);
        }
//        expr.from((NameOrSelect) null, null);
        filter = GoMailModuleSerializer.deserializeDataSourceFilter(new ExprList()
                .add(ExprList.createKeyValue("value", ss))
        );
    }

    private void parse() {
        if (!parsed) {
            parsed = true;
            GoMailDataSource data = dataSource;

            String[] columns = data.getColumns();

            String[] colString = new String[columns.length];
            for (int i = 0; i < colString.length; i++) {
                colString[i] = columns[i];
            }
            sColumns = colString;
            int max = data.getRowCount();
            for (int i = 0; i < max; i++) {
                GoMailDataSourceRow r = data.getRow(i);
                if (filter.accept(getContext(), r)) {
                    rows.add(r);
                }
            }
        }
    }

    @Override
    public String getCell(int rowIndex, int colIndex) {
        parse();
        return rows.get(rowIndex).get(colIndex);
    }

    @Override
    public int getColumnCount() {
        parse();
        return sColumns.length;
    }

    @Override
    public String[] getColumns() {
        parse();
        return sColumns;
    }

    @Override
    public int getRowCount() {
        parse();
        return rows.size();
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
