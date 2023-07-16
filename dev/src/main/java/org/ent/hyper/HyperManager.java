package org.ent.hyper;

public abstract class HyperManager {
    public abstract float getFloat(String propertyName, float minValue, float maxValue);

    public boolean isCollecting() {
        return false;
    }
}
