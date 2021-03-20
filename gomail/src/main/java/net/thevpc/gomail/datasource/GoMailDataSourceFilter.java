/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail.datasource;

import net.thevpc.gomail.GoMailContext;
import net.thevpc.gomail.GoMailDataSourceRow;
import net.thevpc.gomail.util.SerializedForm;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface GoMailDataSourceFilter {

    public boolean accept(GoMailContext context, GoMailDataSourceRow row);

    public SerializedForm serialize();
}
