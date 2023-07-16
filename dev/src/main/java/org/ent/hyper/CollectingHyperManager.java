package org.ent.hyper;

import java.util.ArrayList;
import java.util.List;

public class CollectingHyperManager extends HyperManager {

    private final List<HyperDefinition> hyperDefinitions = new ArrayList<>();

    @Override
    public float getFloat(String propertyName, float minValue, float maxValue) {
        hyperDefinitions.add(new FloatHyperDefinition(propertyName, minValue, maxValue));
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
