/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail;

import java.util.Map;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface GoMailContext {

    public String eval(String expr);

    public GoMailProperties getProperties();

    public Map<String, GoMailDataSource> getRegisteredDataSources();
}
