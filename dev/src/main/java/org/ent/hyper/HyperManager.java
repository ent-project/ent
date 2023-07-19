package org.ent.hyper;

public abstract class HyperManager {
    public abstract double getDouble(String propertyName, double minValue, double maxValue);
    public abstract int getInt(String propertyName, int minValue, int maxValue);

    public boolean isCollecting() {
        return false;
    }
}
