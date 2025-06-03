/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail.datasource;

import net.thevpc.nmail.NMailContext;
import net.thevpc.nmail.expr.Expr;
import net.thevpc.nmail.expr.ExprEvaluator;
import net.thevpc.nmail.expr.ExprVars;
import net.thevpc.nmail.expr.FctExpr;
import net.thevpc.nmail.util.NMailUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author taha.bensalah@gmail.com
 */
public class SimpleXlsNMailDataSource extends AbstractNMailDataSource {

    private List<RowWithId> rows = new ArrayList<>();
    private List<String> columns = new ArrayList<>();
    private Expr arg;
    private String type;

    public SimpleXlsNMailDataSource(Expr arg) {
        this.arg = arg;
    }

    public void build(NMailContext context, ExprVars vars) {
        super.build(context, vars);
        columns.clear();
        rows.clear();
        String cwd = (String) vars.get("cwd");
        String path = new ExprEvaluator().evalExpr(arg, String.class, vars);
        if (path.endsWith(".xlsx")) {
            this.type = "xlsx";
        } else if (path.endsWith(".xls")) {
            this.type = "xls";
        } else {
            throw new IllegalArgumentException("Invalid file type " + path + " (not csv or xlsx)");
        }
        if (NMailUtils.isURL(path)) {
            try (InputStream reader = NMailUtils.toURL(path).openStream()) {
                init(reader);
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        } else {
            Path pp = Paths.get(path);
            if (!pp.isAbsolute() && cwd != null) {
                Path p2 = Paths.get(cwd).resolve(pp);
                if (Files.isRegularFile(p2)) {
                    try (InputStream reader = Files.newInputStream(p2)) {
                        init(reader);
                    } catch (IOException ex) {
                        throw new IllegalArgumentException(ex);
                    }
                    return;
                }
            }
            if (Files.isRegularFile(pp)) {
                try (InputStream reader = Files.newInputStream(pp)) {
                    init(reader);
                } catch (IOException ex) {
                    throw new IllegalArgumentException(ex);
                }
                return;
            }
            throw new IllegalArgumentException("csv path not found " + path);
        }
    }

    private void init(InputStream fis) {

        try (Workbook workbook = WorkbookFactory.create(fis)) {
            for (Sheet sheet : workbook) {
                int firstRowNum = sheet.getFirstRowNum();
                int lastRowNum = sheet.getLastRowNum();

                for (int rowIndex = firstRowNum; rowIndex <= lastRowNum; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        // Entire row is missing (not defined in file)
                        continue;
                    }
                    // Now do fixed-column iteration
                    int lastCellNum = sheet.getRow(firstRowNum).getLastCellNum(); // or fixed number of columns
                    List<String> r = new ArrayList<>();
                    for (int colIndex = 0; colIndex < lastCellNum; colIndex++) {
                        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        String cellValue = (cell == null) ? "" : getCellAsString(cell);
                        r.add(cellValue);
                    }
                    if (!isEmptyRow(r)) {
                        if (columns.isEmpty()) {
                            columns.addAll(r);
                        } else {
                            rows.add(new RowWithId(rowIndex, r));
                        }
                    }
                }
                break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isEmptyRow(List<String> r) {
        for (String s : r) {
            if (!s.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String getCellAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return Double.toString(cell.getNumericCellValue());
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula(); // or evaluate if needed
            case BLANK:
                return "";
            default:
                return "?";
        }
    }

    @Override
    public String getColumn(int index) {
        return columns.get(index);
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public String getCell(int rowIndex, int colIndex) {
        if (rowIndex >= 0 && rowIndex < rows.size()) {
            if (colIndex >= 0 && colIndex < rows.get(rowIndex).getValues().size()) {
                return rows.get(rowIndex).getValues().get(colIndex);
            }
        }
        return null;
    }

    @Override
    public String getRowId(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < rows.size()) {
            return rows.get(rowIndex).getRowId();
        }
        return null;
    }

    @Override
    public Expr toExpr() {
        return new FctExpr("xls", new Expr[]{arg});
    }

}
