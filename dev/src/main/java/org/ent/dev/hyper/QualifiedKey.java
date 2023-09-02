package org.ent.dev.hyper;

public class QualifiedKey {
    public final String path;
    public final String simpleKey;

    public QualifiedKey(String path, String simpleKey) {
        this.path = path;
        this.simpleKey = simpleKey;
    }

    public String extendPath(String group) {
        if (path.isEmpty()) {
            return group;
        } else {
            return path + "." + group;
        }
    }

    public String get() {
        if (path.isEmpty()) {
            return simpleKey;
        } else {
            return path + "." + simpleKey;
        }
    }
}
