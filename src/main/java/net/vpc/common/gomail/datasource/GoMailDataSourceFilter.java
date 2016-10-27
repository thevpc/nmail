/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail.datasource;

import net.vpc.common.gomail.GoMailContext;
import net.vpc.common.gomail.GoMailDataSourceRow;
import net.vpc.common.gomail.util.SerializedForm;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface GoMailDataSourceFilter {

    public boolean accept(GoMailContext context, GoMailDataSourceRow row);

    public SerializedForm serialize();
}
