package net.thevpc.nmail.modules;

import net.thevpc.nmail.NMailProperties;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * Created by vpc on 7/5/16.
 */
class DefaultProps implements NMailProperties {

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

    @Override
    public boolean containsKeyIgnoreCase(String name) {
        for (Properties a : all) {
            if (a.containsKey(name)) {
                return true;
            }
        }
        for (Properties a : all) {
            if (a.keySet().stream().map(x -> x == null ? null : x.toString().toLowerCase()).anyMatch(x -> Objects.equals(x, name))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPropertyIgnoreCase(String name) {
        for (Properties a : all) {
            String v = a.getProperty(name);
            if (v != null) {
                return v;
            }
        }
        for (Properties a : all) {
            Map<String, Object> o = asLowerCaseKey((Map) a);
            String v = (String) o.get(name);
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    @Override
    public Set<String> keySetLowerCased() {
        return keySet().stream().map(x->x==null?null:x.toLowerCase()).collect(Collectors.toSet());
    }

    @Override
    public Set<String> keySet() {
        Set<String> all = new HashSet<>();
        for (Properties properties : this.all) {
            for (Object o : properties.keySet()) {
                if (o == null) {
                    all.add(null);
                }
                all.add(o.toString().toLowerCase());
            }
        }
        return all;
    }

    private Map<String, Object> asLowerCaseKey(Map<String, Object> a) {
        return a.entrySet().stream().map(x ->
                new AbstractMap.SimpleEntry<>(
                        x == null ? null : x.getKey().toString().toLowerCase(),
                        x.getValue()
                )
        ).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue(), new BinaryOperator<Object>() {
            @Override
            public Object apply(Object o, Object o2) {
                return o;
            }
        }));
    }

    ;
}
