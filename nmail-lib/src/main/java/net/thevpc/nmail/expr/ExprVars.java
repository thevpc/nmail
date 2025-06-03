package net.thevpc.nmail.expr;

import java.util.Map;
import java.util.Set;

public interface ExprVars {
    boolean containsKey(String key);
    Object get(String key);
    void put(String key,Object value);
    void putAll(Map<String, Object> rowVars);
    ExprVars copy();

    Set<String> keySet();
}
