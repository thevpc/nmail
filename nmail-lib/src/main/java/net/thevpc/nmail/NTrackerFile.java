package net.thevpc.nmail;

import java.io.*;
import java.util.Map;
import java.util.Properties;

public class NTrackerFile implements NTracker {
    private String path;
    private Map<String, Status> statuses = new java.util.HashMap<>();

    public NTrackerFile(String path) {
        this.path = path;
        load();
    }

    @Override
    public synchronized void setRowStatus(String rowId, Status status) {
        if (status == null) {
            statuses.remove(rowId);
        } else {
            statuses.put(rowId, status);
        }
        save();
    }

    @Override
    public synchronized Status getRowStatus(String rowId) {
        Status u = statuses.get(rowId);
        return u == null ? Status.TODO : u;
    }

    private synchronized void load() {
        File f = new File(path);
        if (f.isFile()) {
            Properties p = new Properties();
            try (InputStream in = new FileInputStream(f)) {
                p.load(in);
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
            for (Map.Entry<Object, Object> e : p.entrySet()) {
                String k = e.getKey() == null ? null : e.getKey().toString();
                String v = e.getValue() == null ? null : e.getValue().toString();
                if (k != null && v != null) {
                    statuses.put(k, Status.valueOf(v.trim().toUpperCase()));
                }
            }
        }
    }

    private synchronized void save() {
        Properties p = new Properties();
        for (Map.Entry<String, Status> e : statuses.entrySet()) {
            p.setProperty(e.getKey(), e.getValue().name());
        }
        File f = new File(path);
        File parent = f.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(f)) {
            p.store(out, "");
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
