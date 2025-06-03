package net.thevpc.nmail;

import java.util.Set;

/**
 * Created by vpc on 7/5/16.
 */
public interface NMailProperties {

    public String getProperty(String name);

    boolean containsKeyIgnoreCase(String name);

    public String getPropertyIgnoreCase(String name);
    public Set<String> keySet();
    public Set<String> keySetLowerCased();
}
