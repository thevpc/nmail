/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.upa;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Base64;
import net.thevpc.gomail.util.SerializedForm;
import net.thevpc.upa.UPA;
import net.thevpc.upa.bulk.DataReader;
import net.thevpc.upa.bulk.ParseFormatManager;
import net.thevpc.upa.bulk.XmlParser;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class XMLGoMailDataSource extends AbstractDataParserGoMailDataSource {

    public static XMLGoMailDataSource valueOf(SerializedForm form) {
        String v = form.getValue();
        if (v.startsWith("bytes:")) {
            byte[] b = Base64.getDecoder().decode(v.substring("bytes:".length()));
            return new XMLGoMailDataSource(b);
        } else {
            return new XMLGoMailDataSource(v);
        }
    }

    public XMLGoMailDataSource(byte[] bytes) {
        super(bytes);
    }

    public XMLGoMailDataSource(File file) {
        super(file);
    }

    public XMLGoMailDataSource(URL url) {
        super(url);
    }

    public XMLGoMailDataSource(String url) {
        super(url);
    }

    public XMLGoMailDataSource(Reader reader) {
        super(reader);
    }

    public XMLGoMailDataSource(InputStream reader) {
        super(reader);
    }

    @Override
    protected DataReader createDataTable() throws IOException {
        Object source = getBuildSource();
        XmlParser parser = UPA.getBootstrap().getFactory().createObject(ParseFormatManager.class).createXmlParser(source);
        parser.setContainsHeader(true);
        return parser.parse();
    }

}
