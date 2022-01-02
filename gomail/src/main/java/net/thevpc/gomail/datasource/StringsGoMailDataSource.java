///*
// * To change this license header, choose License Headers in Project Properties.
// *
// * and open the template in the editor.
// */
//package net.thevpc.gomail.datasource;
//
//import net.thevpc.gomail.expr.Expr;
//import net.thevpc.gomail.expr.ExprHelper;
//import net.thevpc.gomail.util.GoMailUtils;
//import net.thevpc.gomail.util.SerializedForm;
//
///**
// * @author taha.bensalah@gmail.com
// */
//public class StringsGoMailDataSource extends AbstractGoMailDataSource {
//
//    private String[] columns;
//
//    public StringsGoMailDataSource(String[][] values, String[] columns) {
//        super(values);
//        this.columns = columns;
//    }
//
//    @Override
//    public SerializedForm serialize() {
//        StringBuilder sb = new StringBuilder("{");
//
//        sb.append('[');
//        for (String column : columns) {
//            sb.append(GoMailUtils.escapeString(column));
//        }
//        sb.append(']');
//
//        sb.append('[');
//        String[][] source = (String[][]) getSource();
//        for (int i = 0; i < source.length; i++) {
//            String[] row = source[i];
//            if (i > 0) {
//                sb.append(',');
//            }
//            sb.append('[');
//            for (int j = 0; j < row.length; j++) {
//                if (j > 0) {
//                    sb.append(',');
//                }
//                sb.append(GoMailUtils.escapeString(row[j]));
//            }
//            sb.append(']');
//        }
//        sb.append(']');
//
//        sb.append('}');
//        return new SerializedForm(
//                new Expr[]{
//                        ExprHelper.assign("type","strings"),
//                        ExprHelper.assign("value",sb.toString())
//                }
//        );
//    }
//
//
//    @Override
//    public String getCell(int rowIndex, int colIndex) {
//        String[][] values = (String[][]) getBuildSource();
//        return values[rowIndex][colIndex];
//    }
//
//    @Override
//    public int getColumnCount() {
//        return columns.length;
//    }
//
//    @Override
//    public int getRowCount() {
//        String[][] values = (String[][]) getBuildSource();
//        return values.length;
//    }
//
//    @Override
//    public String[] getColumns() {
//        return columns;
//    }
//
//}
