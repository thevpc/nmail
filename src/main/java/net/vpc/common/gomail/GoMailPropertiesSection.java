/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail;

import java.util.Properties;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class GoMailPropertiesSection {
    private int pragmaPriority;
    private String filterRegexp;
    private Properties properties;

    public GoMailPropertiesSection() {
    }

    public int getPragmaPriority() {
        return pragmaPriority;
    }

    public void setPragmaPriority(int pragmaPriority) {
        this.pragmaPriority = pragmaPriority;
    }

    public String getFilterRegexp() {
        return filterRegexp;
    }

    public void setFilterRegexp(String filterRegexp) {
        this.filterRegexp = filterRegexp;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
}
