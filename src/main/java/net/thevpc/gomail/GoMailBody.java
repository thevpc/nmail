/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface GoMailBody {

    public void setPosition(GoMailBodyPosition position);

    public GoMailBodyPosition getPosition();

    public void setOrder(int order);

    public int getOrder();

    public boolean isExpandable();

    public void setExpandable(boolean expandable);

    public String getContentType();

    public void setContentType(String contentType);

    public GoMailBody copy();
}
