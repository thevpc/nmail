/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.thevpc.nmail.util.NMailUtils;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class NMailBodyList implements Iterable<NMailBody> {

    private List<NMailBody> list = new ArrayList<>();

    public NMailBody get(int i) {
        return list.get(i);
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void add(Iterable<NMailBody> other) {
        for(NMailBody a:other){
            list.add(a);
        }
    }

    public void add(byte[] content, String contentType, boolean expandable) {
        add(new NMailBodyContent(content, contentType, expandable));
    }

    public void add(String content, String contentType, boolean expandable) {
        add(new NMailBodyContent(content.getBytes(), contentType, expandable));
    }

    public void add(InputStream content, String contentType, boolean expandable) throws IOException {
        add(new NMailBodyContent(NMailUtils.loadByteArray(content), contentType, expandable));
    }

    public void add(File content, String contentType, boolean expandable) throws IOException {
        add(new NMailBodyPath(content.toURI().toURL().toString(), contentType, expandable));
    }

    public void add(URL content, String contentType, boolean expandable) throws IOException {
        add(new NMailBodyPath(content.toString(), contentType, expandable));
    }

    public void add(NMailBody body) {
        list.add(body);
    }

    @Override
    public Iterator<NMailBody> iterator() {
        return list.iterator();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.list);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NMailBodyList other = (NMailBodyList) obj;
        if (!Objects.equals(this.list, other.list)) {
            return false;
        }
        return true;
    }

    public NMailBodyList copy(){
        NMailBodyList o=new NMailBodyList();
        for (NMailBody x : this) {
            o.list.add(x==null?null:x.copy());
        }
        return o;
    }
}
