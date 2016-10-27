/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail;

import java.util.Arrays;

import net.vpc.common.gomail.util.GoMailUtils;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class GoMailBodyContent extends AbstractGoMailBody implements Cloneable {

    private byte[] value;

    public GoMailBodyContent(byte[] value, String contentType, boolean expandable) {
        super(contentType, expandable);
        this.value = value;
    }

    @Override
    public GoMailBodyContent copy() {
        GoMailBodyContent r = (GoMailBodyContent) super.copy();
        if (r.value != null) {
            r.value = new byte[r.value.length];
            System.arraycopy(value, 0, r.value, 0, value.length);
        }
        return r;
    }

    public byte[] getByteArray() {
        return value;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 53 * hash + Arrays.hashCode(this.value);
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
        final GoMailBodyContent other = (GoMailBodyContent) obj;
        if (!Arrays.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String s = null;
        if (getContentType() == null ||
                GoMailUtils.isTextContentType(getContentType())) {
            s = (value == null) ? "" : new String(value);
        } else {
            s = (value == null) ? "" : ("binary<" + value.length + ">");
        }
        return "body{" + getContentType() + ";" + getOrder() + ";" + getPosition() + " ; content=" + s + '}';
    }

}
