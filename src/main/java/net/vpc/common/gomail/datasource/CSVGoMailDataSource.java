/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail.datasource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Base64;
import net.vpc.common.gomail.util.ExprList;
import net.vpc.common.gomail.util.SerializedForm;
import net.vpc.upa.UPA;
import net.vpc.upa.bulk.DataReader;
import net.vpc.upa.bulk.ParseFormatManager;
import net.vpc.upa.bulk.TextCSVParser;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class CSVGoMailDataSource extends AbstractDataParserGoMailDataSource {

    public static CSVGoMailDataSource valueOf(SerializedForm form) {
        ExprList.Expr rl = form.getArgs().searchValueByKey("readlines");
        if (rl != null) {
            return new CSVGoMailDataSource(rl.asString().getBytes());
        }
        String v = form.getValue();
        if (v.startsWith("bytes:")) {
            byte[] b = Base64.getDecoder().decode(v.substring("bytes:".length()));
            return new CSVGoMailDataSource(b);
        } else if (v.startsWith("text:")) {
            byte[] b = v.substring("text:".length()).trim().getBytes();
            return new CSVGoMailDataSource(b);
        } else {
            return new CSVGoMailDataSource(v);
        }
    }

    public CSVGoMailDataSource(byte[] bytes) {
        super(bytes);
    }

    public CSVGoMailDataSource(File file) {
        super(file);
    }

    public CSVGoMailDataSource(URL url) {
        super(url);
    }

    public CSVGoMailDataSource(String url) {
        super(url);
    }

    public CSVGoMailDataSource(Reader reader) {
        super(reader);
    }

    public CSVGoMailDataSource(InputStream reader) {
        super(reader);
    }

    @Override
    protected DataReader createDataTable() throws IOException {
        Object source = getBuildSource();
        if (source instanceof byte[]) {
            source = new ByteArrayInputStream((byte[]) source);
        }
        TextCSVParser parser = UPA.getBootstrap().getFactory().createObject(ParseFormatManager.class).createTextCSVParser(source);
        parser.setContainsHeader(true);
        return parser.parse();
    }

}
