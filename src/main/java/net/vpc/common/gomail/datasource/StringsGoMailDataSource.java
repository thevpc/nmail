/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail.datasource;

import net.vpc.common.gomail.util.ExprList;
import net.vpc.common.gomail.util.SerializedForm;

/**
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
        StringBuilder sb = new StringBuilder("{");

        sb.append('[');
        for (String column : columns) {
            sb.append(escape(column));
        }
        sb.append(']');

        sb.append('[');
        String[][] source = (String[][]) getSource();
        for (int i = 0; i < source.length; i++) {
            String[] row = source[i];
            if (i > 0) {
                sb.append(',');
            }
            sb.append('[');
            for (int j = 0; j < row.length; j++) {
                if (j > 0) {
                    sb.append(',');
                }
                sb.append(escape(row[j]));
            }
            sb.append(']');
        }
        sb.append(']');

        sb.append('}');
        return new SerializedForm(new ExprList().addAll(
            ExprList.createKeyValue("type","strings"),
            ExprList.createKeyValue("value",sb.toString())
        ));
    }

    private String escape(String s) {
        if (s == null) {
            return "null";
        } else {
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray()) {
                switch (c) {
                    case '\'':
                    case '\\': {
                        sb.append('\\').append(c);
                        break;
                    }
                    case '\n': {
                        sb.append("\\n");
                        break;
                    }
                    case '\t': {
                        sb.append("\\t");
                        break;
                    }
                    case '\r': {
                        sb.append("\\r");
                        break;
                    }
                    case '\f': {
                        sb.append("\\f");
                        break;
                    }
                    default: {
                        if ((c < 0x0020) || (c > 0x007e)) {
                            sb.append('\\');
                            sb.append('u');
                            sb.append(toHex((c >> 12) & 0xF));
                            sb.append(toHex((c >> 8) & 0xF));
                            sb.append(toHex((c >> 4) & 0xF));
                            sb.append(toHex(c & 0xF));
                        } else {
                            sb.append(c);
                        }
                    }
                }
            }
            return sb.toString();
        }
    }

    private static final char[] hexDigit = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static char toHex(int nibble) {
        return hexDigit[nibble & 0xf];
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
