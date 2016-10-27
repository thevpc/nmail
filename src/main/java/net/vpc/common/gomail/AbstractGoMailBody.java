/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail;

import java.util.Objects;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public abstract class AbstractGoMailBody implements GoMailBody {

    private String contentType;
    private boolean expandable;
    private int order;
    private GoMailBodyPosition position = GoMailBodyPosition.OBJECT;

    public AbstractGoMailBody(String contentType, boolean expandable) {
        this.contentType = contentType;
        this.expandable = expandable;
    }

    @Override
    public GoMailBodyPosition getPosition() {
        return position;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    @Override
    public void setPosition(GoMailBodyPosition position) {
        this.position = position;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean isExpandable() {
        return expandable;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.contentType);
        hash = 29 * hash + (this.expandable ? 1 : 0);
        hash = 29 * hash + Objects.hashCode(this.position);
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
        final AbstractGoMailBody other = (AbstractGoMailBody) obj;
        if (!Objects.equals(this.contentType, other.contentType)) {
            return false;
        }
        if (this.expandable != other.expandable) {
            return false;
        }
        if (this.order != other.order) {
            return false;
        }
        if (this.position != other.position) {
            return false;
        }
        return true;
    }
    
    
    @Override
    public AbstractGoMailBody copy() {
        try {
            return (AbstractGoMailBody) clone();
        } catch (CloneNotSupportedException ex) {
            throw new IllegalArgumentException("Never");
        }
    }
    
}
