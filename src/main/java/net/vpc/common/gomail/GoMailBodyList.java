/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.vpc.common.io.IOUtils;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class GoMailBodyList implements Iterable<GoMailBody> {

    private List<GoMailBody> list = new ArrayList<>();

    public GoMailBody get(int i) {
        return list.get(i);
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void add(Iterable<GoMailBody> other) {
        for(GoMailBody a:other){
            list.add(a);
        }
    }

    public void add(byte[] content, String contentType, boolean expandable) {
        add(new GoMailBodyContent(content, contentType, expandable));
    }

    public void add(String content, String contentType, boolean expandable) {
        add(new GoMailBodyContent(content.getBytes(), contentType, expandable));
    }

    public void add(InputStream content, String contentType, boolean expandable) throws IOException {
        add(new GoMailBodyContent(IOUtils.loadByteArray(content), contentType, expandable));
    }

    public void add(File content, String contentType, boolean expandable) throws IOException {
        add(new GoMailBodyPath(content.toURI().toURL().toString(), contentType, expandable));
    }

    public void add(URL content, String contentType, boolean expandable) throws IOException {
        add(new GoMailBodyPath(content.toString(), contentType, expandable));
    }

    public void add(GoMailBody body) {
        list.add(body);
    }

    @Override
    public Iterator<GoMailBody> iterator() {
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
        final GoMailBodyList other = (GoMailBodyList) obj;
        if (!Objects.equals(this.list, other.list)) {
            return false;
        }
        return true;
    }

    public GoMailBodyList copy(){
        GoMailBodyList o=new GoMailBodyList();
        for (GoMailBody x : this) {
            o.list.add(x==null?null:x.copy());
        }
        return o;
    }
}
