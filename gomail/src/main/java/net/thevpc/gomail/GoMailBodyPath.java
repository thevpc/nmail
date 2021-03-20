/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail;

import java.util.Objects;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class GoMailBodyPath extends AbstractGoMailBody implements Cloneable {

    private String path;

    public GoMailBodyPath(String value, String contentType, boolean expandable) {
        super(contentType, expandable);
        this.path = value;
    }


    public String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 31 * hash + Objects.hashCode(this.path);
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
        if (!super.equals(obj)) {
            return false;
        }
        final GoMailBodyPath other = (GoMailBodyPath) obj;
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "body{" + getContentType() + ";" + getOrder() + ";" + getPosition() + " ; path=" + path + '}';
    }

}
