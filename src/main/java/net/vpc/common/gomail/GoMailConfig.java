/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class GoMailConfig {

    private static final Logger log = Logger.getLogger(GoMailConfig.class.getName());
    private List<GoMailPropertiesSection> sections = new ArrayList<>();
    private static GoMailConfig defaultInstance;

    public static GoMailConfig getDefaultInstance() {
        if(defaultInstance==null){
            try {
                defaultInstance=new GoMailConfig(null);
            } catch (IOException e) {
                throw new RuntimeException("Error Loading config",e);
            }
        }
        return defaultInstance;
    }

    public GoMailConfig() {

    }

    public GoMailConfig(ClassLoader loader) throws IOException {
        load(loader);
    }

    public void load(ClassLoader loader) throws IOException {
        load("net/vpc/common/gomail/gomail.properties", loader);
    }

    public void load(String url, ClassLoader loader) throws IOException {
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        for (URL u : Collections.list(loader.getResources(url))) {
            InputStream s = null;
            try {
                s = u.openStream();
                loadStream(s, true);
            } finally {
                if (s != null) {
                    s.close();
                }
            }
        }
    }

    private void loadStream(InputStream in, boolean lenient) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        String line = null;
        final int ANY = 1;
        final int SECTION = 2;
        int status = ANY;
        int priority = 0;
        String regexp = null;
        StringBuilder sb = new StringBuilder();
        while ((line = r.readLine()) != null) {
            switch (status) {
                case ANY: {
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
                                                log.severe("Unvalid priority "+t[1]+" : "+e.getMessage());
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
                            if (line.endsWith("]")) {
                                regexp = line.substring(1, line.length() - 1);
                                status = SECTION;
                            } else if (lenient) {
                                regexp = line.substring(1, line.length());
                                status = SECTION;
                                log.severe("Expected Section [...]");
                            } else {
                                throw new IllegalArgumentException("Expected Section [...]");
                            }
                        } else if (lenient) {
                            log.severe("Expected Section [...]");
                        } else {
                            throw new IllegalArgumentException("Expected Section [...]");
                        }
                    }
                    break;
                }
                case SECTION: {
                    if (line.startsWith("[")) {
                        //consume
                        GoMailPropertiesSection sec = new GoMailPropertiesSection();
                        sec.setFilterRegexp(regexp);
                        sec.setPragmaPriority(priority);
                        sec.setProperties(loadProperties(sb.toString()));
                        sections.add(sec);
                        //reset
                        regexp = null;
                        priority = 0;
                        sb.delete(0, sb.length());

                        //
                        regexp = line.substring(1, line.length() - 1);
                        status = SECTION;
                    } else {
                        sb.append(line).append("\n");
                    }
                    break;
                }
            }
        }
        if (regexp != null) {
            //consume
            GoMailPropertiesSection sec = new GoMailPropertiesSection();
            sec.setFilterRegexp(regexp);
            sec.setPragmaPriority(priority);
            sec.setProperties(loadProperties(sb.toString()));
            sections.add(sec);
        }

    }

    private String[] loadLineProperty(String line) {
        Properties p = loadProperties(line);
        if (p.size() > 0) {
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
            Logger.getLogger(GoMailConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        return p;
    }

    public Properties findConfig(String email) {
        Properties p = new Properties();
        int priority = Integer.MIN_VALUE;
        for (GoMailPropertiesSection s : sections) {
            if (s.getPragmaPriority() >= priority && email.matches(s.getFilterRegexp())) {
                p.putAll(s.getProperties());
            }
        }
        return p;
    }
}
