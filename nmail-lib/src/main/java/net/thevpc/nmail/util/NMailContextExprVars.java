package net.thevpc.nmail.util;

import net.thevpc.nmail.NMailContext;
import net.thevpc.nmail.expr.ExprVars;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class NMailContextExprVars implements ExprVars {
    private NMailContext context;
    private Map<String, Object> map;

    public NMailContextExprVars(NMailContext context, Map<String, Object> map) {
        this.context = context;
        this.map = map;
    }

    @Override
    public boolean containsKey(String key) {
        String k = key == null ? null : key.toLowerCase();
        if (map.containsKey(k)) {
            return true;
        }
        if (context.getProperties().containsKeyIgnoreCase(key)) {
            return true;
        }
        return false;
    }

    @Override
    public Object get(String key) {
        String k = key == null ? null : key.toLowerCase();
        Object v = map.get(k);
        if (v != null) {
            return v;
        }
        v = context.getProperties().getPropertyIgnoreCase(key);
        return v;
    }

    @Override
    public void put(String key, Object value) {
        map.put(key == null ? null : key.toLowerCase(), value);
    }

    @Override
    public void putAll(Map<String, Object> rowVars) {
        for (Map.Entry<String, Object> e : rowVars.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public ExprVars copy() {
        return new NMailContextExprVars(context, new HashMap<>(map));
    }

    @Override
    public Set<String> keySet() {
        LinkedHashSet lhs = new LinkedHashSet();
        lhs.addAll(map.keySet());
        lhs.addAll(context.getProperties().keySetLowerCased());
        return lhs;
    }
}
