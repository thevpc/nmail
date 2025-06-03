/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail;

import java.util.Properties;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class NMailPropertiesSection {
    private int pragmaPriority;
    private String filterType;
    private String filterRegexp;
    private Properties properties;

    public NMailPropertiesSection() {
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

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }
    

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
}
