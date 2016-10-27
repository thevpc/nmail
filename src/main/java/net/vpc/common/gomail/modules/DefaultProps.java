package net.vpc.common.gomail.modules;

import net.vpc.common.gomail.GoMailProperties;

import java.util.Properties;

/**
 * Created by vpc on 7/5/16.
 */
class DefaultProps implements GoMailProperties {

    private Properties[] all;

    public DefaultProps(Properties... all) {
        this.all = all;
    }

    public String getProperty(String name) {
        for (Properties a : all) {
            String v = a.getProperty(name);
            if (v != null) {
                return v;
            }
        }
        return null;
    }
}
