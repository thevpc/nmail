/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.datasource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import net.thevpc.gomail.util.SerializedForm;
import net.thevpc.upa.UPA;
import net.thevpc.upa.bulk.DataReader;
import net.thevpc.upa.bulk.ParseFormatManager;
import net.thevpc.upa.bulk.SheetParser;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class SheetGoMailDataSource extends AbstractDataParserGoMailDataSource {

//    private SheetParser parser;
    public static SheetGoMailDataSource valueOf(SerializedForm form) {
        String v = form.getValue();
        if (v.startsWith("bytes:")) {
            byte[] b = Base64.getDecoder().decode(v.substring("bytes:".length()));
            return new SheetGoMailDataSource(b);
        } else {
            return new SheetGoMailDataSource(v);
        }
    }

    public SheetGoMailDataSource(byte[] bytes) {
        super(bytes);
    }

    public SheetGoMailDataSource(String file) {
        super(file);
    }

    public SheetGoMailDataSource(File file) {
        super(file);
    }

    public SheetGoMailDataSource(URL url) {
        super(url);
    }

    public SheetGoMailDataSource(InputStream reader) {
        super(reader);
    }

    @Override
    protected DataReader createDataTable() throws IOException {
        Object source = getBuildSource();
        if(source instanceof String){
            source=new URL((String)source);
        }
        SheetParser parser = UPA.getBootstrap().getFactory().createObject(ParseFormatManager.class).createSheetParser(source);
        parser.setContainsHeader(true);
        return parser.parse();
    }

}
