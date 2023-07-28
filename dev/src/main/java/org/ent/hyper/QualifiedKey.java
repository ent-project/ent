package org.ent.hyper;

public class QualifiedKey {
    private final String qualified;

    public QualifiedKey(String qualified) {
        this.qualified = qualified;
    }

    public String get() {
        return qualified;
    }
}
