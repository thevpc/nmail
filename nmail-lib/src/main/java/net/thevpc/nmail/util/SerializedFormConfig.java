/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class SerializedFormConfig {

    private List<String> imports = new ArrayList<String>();
    private Map<String, String> typeMappings = new HashMap<>();

    public SerializedFormConfig addAlias(String t1, String t2) {
        typeMappings.put(t1, t2);
        return this;
    }

    public SerializedFormConfig addImport(String i) {
        imports.add(i);
        return this;
    }

    public List<String> getImports() {
        return imports;
    }

    public Set<String> getAliases() {
        return typeMappings.keySet();
    }

    public Set<String> getAliasesFor(String type) {
        Set<String> found = new HashSet<>();
        for (Map.Entry<String, String> en : typeMappings.entrySet()) {
            if (en.getValue().equals(type)) {
                found.add(en.getKey());
            }
        }
        return found;
    }

    public String getAlias(String name) {
        return typeMappings.get(name);
    }

}
