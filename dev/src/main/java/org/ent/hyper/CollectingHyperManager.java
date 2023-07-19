package org.ent.hyper;

import java.util.ArrayList;
import java.util.List;

public class CollectingHyperManager extends HyperManager {

    private final List<HyperDefinition> hyperDefinitions = new ArrayList<>();

    @Override
    public double getDouble(String propertyName, double minValue, double maxValue) {
        hyperDefinitions.add(new DoubleHyperDefinition(propertyName, minValue, maxValue));
        return (minValue + maxValue) / 2;
    }

    @Override
    public int getInt(String propertyName, int minValue, int maxValue) {
        hyperDefinitions.add(new IntHyperDefinition(propertyName, minValue, maxValue));
        return (minValue + maxValue) / 2;
    }

    @Override
    public boolean isCollecting() {
        return true;
    }

    public List<HyperDefinition> getHyperDefinitions() {
        return hyperDefinitions;
    }
}
