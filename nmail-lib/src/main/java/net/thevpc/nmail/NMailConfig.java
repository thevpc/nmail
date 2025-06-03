/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail;

import net.thevpc.nuts.util.NBlankable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author taha.bensalah@gmail.com
 */
public class NMailConfig {

    private static final Logger log = Logger.getLogger(NMailConfig.class.getName());
    private static NMailConfig defaultInstance;
    private List<NMailPropertiesSection> sections = new ArrayList<>();

    public NMailConfig() {

    }

    public NMailConfig(ClassLoader loader) {
        load(loader);
    }

    public static NMailConfig getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new NMailConfig(null);
        }
        return defaultInstance;
    }

    public void load(ClassLoader loader) {
        load("net/thevpc/nmail/nmail.properties", loader);
    }

    public void load(String url, ClassLoader loader) {
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        try {
            for (URL u : Collections.list(loader.getResources(url))) {
                try (InputStream s = u.openStream()) {
                    loadStream(s, true);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Cond parseCond(String line, boolean lenient) {
        if (line != null) {
            if (line.startsWith("[")) {
                if (line.endsWith("]")) {
                    String regexp0 = line.substring(1, line.length() - 1);
                    int eq = regexp0.indexOf('=');
                    if (eq < 0) {
                        Cond cc = new Cond();
                        cc.name = "";
                        cc.expression = regexp0.substring(eq + 1);
                        return cc;
                    }
                    Cond cc = new Cond();
                    cc.name = regexp0.substring(0, eq);
                    cc.expression = regexp0.substring(eq + 1);
                    return cc;
                } else if (lenient) {
                    String regexp0 = line.substring(1, line.length());
                    int eq = regexp0.indexOf('=');
                    if (eq < 0) {
                        Cond cc = new Cond();
                        cc.name = "";
                        cc.expression = regexp0.substring(eq + 1);
                        return cc;
                    }
                    Cond cc = new Cond();
                    cc.name = regexp0.substring(0, eq);
                    cc.expression = regexp0.substring(eq + 1);
                    log.severe("Expected Section [...]");
                } else {
                    throw new IllegalArgumentException("Expected Section [...]");
                }
            }
        }
        throw new IllegalArgumentException("Expected Section [...]");
    }

    private void loadStream(InputStream in, boolean lenient) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String line = null;
            int priority = 0;
            NMailPropertiesSection sec = null;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    if (line.startsWith("#")) {
                        if (line.matches("#pragma\\s+.*")) {
                            String[] t = loadLineProperty(line.substring("#pragma".length() + 1));
                            if (t != null) {
                                if (t[0].equals("priority")) {
                                    try {
                                        priority = Integer.parseInt(t[1]);
                                    } catch (NumberFormatException e) {
                                        if (lenient) {
                                            log.severe("Invalid priority " + t[1] + " : " + e.getMessage());
                                        } else {
                                            throw e;
                                        }
                                    }
                                } else if (lenient) {
                                    log.severe("Pragma " + t[0] + " not supported");
                                } else {
                                    throw new IllegalArgumentException("Pragma " + t[0] + " not supported");
                                }
                            }
                        }
                    } else if (line.startsWith("[")) {
                        Cond cond = parseCond(line, lenient);
                        sec = new NMailPropertiesSection();
                        sec.setFilterType(cond.name);
                        sec.setFilterRegexp(cond.expression);
                        sec.setPragmaPriority(priority);
                        sec.setProperties(loadProperties(""));
                        sections.add(sec);
                        //reset
                        priority = 0;
                    } else if (!NBlankable.isBlank(line) && sec != null) {
                        Properties props = loadProperties(line.trim());
                        if (props.isEmpty()) {
                            if (lenient) {
                                log.severe("Expected Section [...]");
                            } else {
                                throw new IllegalArgumentException("Expected Section [...]");
                            }
                        } else {
                            sec.getProperties().putAll(props);
                        }
                    } else if (lenient) {
                        log.severe("Expected Section [...]");
                    } else {
                        throw new IllegalArgumentException("Expected Section [...]");
                    }
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private String[] loadLineProperty(String line) {
        Properties p = loadProperties(line);
        if (!p.isEmpty()) {
            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                return new String[]{String.valueOf(entry.getKey()), String.valueOf(entry.getValue())};
            }
        }
        return null;
    }

    private Properties loadProperties(String string) {
        Properties p = new Properties();
        try {
            StringReader r = new StringReader(string);
            p.load(r);
            return p;
        } catch (IOException ex) {
            Logger.getLogger(NMailConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        return p;
    }

    public Properties findConfig(String email, String provider) {
        Properties p = new Properties();
        int priority = Integer.MIN_VALUE;
        for (NMailPropertiesSection s : sections) {
            if (s.getPragmaPriority() >= priority) {
                boolean accept = false;
                String ft = s.getFilterType();
                if (ft == null || ft.isEmpty() || ft.equals("mail")) {
                    if (email != null && email.matches(s.getFilterRegexp())) {
                        accept = true;
                    }
                } else if (ft.equals("provider")) {
                    if (provider != null && provider.matches(s.getFilterRegexp())) {
                        accept = true;
                    }
                }
                if (accept) {
                    p.putAll(s.getProperties());
                }
            }
        }
        return p;
    }

    private static class Cond {

        String name;
        String expression;
    }
}
