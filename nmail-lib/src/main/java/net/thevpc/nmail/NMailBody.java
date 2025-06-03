/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface NMailBody {

    public void setPosition(NMailBodyPosition position);

    public NMailBodyPosition getPosition();

    public void setOrder(int order);

    public int getOrder();

    public boolean isExpandable();

    public void setExpandable(boolean expandable);

    public String getContentType();

    public void setContentType(String contentType);

    public NMailBody copy();
}
