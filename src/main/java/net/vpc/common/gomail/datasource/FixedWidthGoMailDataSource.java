/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail.datasource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Base64;
import net.vpc.common.gomail.util.SerializedForm;
import net.vpc.upa.UPA;
import net.vpc.upa.bulk.DataReader;
import net.vpc.upa.bulk.ParseFormatManager;
import net.vpc.upa.bulk.TextFixedWidthParser;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class FixedWidthGoMailDataSource extends AbstractDataParserGoMailDataSource {

    public static FixedWidthGoMailDataSource valueOf(SerializedForm form) {
        String v = form.getValue();
        if (v.startsWith("bytes:")) {
            byte[] b = Base64.getDecoder().decode(v.substring("bytes:".length()));
            return new FixedWidthGoMailDataSource(b);
        } else {
            return new FixedWidthGoMailDataSource(v);
        }
    }

    public FixedWidthGoMailDataSource(byte[] bytes) {
        super(bytes);
    }

    public FixedWidthGoMailDataSource(File file) {
        super(file);
    }

    public FixedWidthGoMailDataSource(String file) {
        super(file);
    }

    public FixedWidthGoMailDataSource(URL url) {
        super(url);
    }

    public FixedWidthGoMailDataSource(Reader reader) {
        super(reader);
    }

    public FixedWidthGoMailDataSource(InputStream reader) {
        super(reader);
    }

    @Override
    protected DataReader createDataTable() throws IOException {
        Object source = getBuildSource();
        TextFixedWidthParser parser = UPA.getBootstrap().getFactory().createObject(ParseFormatManager.class).createTextFixedWidthParser(source);
        parser.setContainsHeader(true);
        return parser.parse();
    }

}
