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
    public double get(DoubleHyperDefinition hyperDefinition) {
        hyperDefinitions.add(hyperDefinition);
        return (hyperDefinition.getMinValue() + hyperDefinition.getMaxValue()) / 2;
    }

    @Override
    public int get(IntHyperDefinition hyperDefinition) {
        hyperDefinitions.add(hyperDefinition);
        return (hyperDefinition.getMinValue() + hyperDefinition.getMaxValue()) / 2;
    }

    public List<HyperDefinition> getHyperDefinitions() {
        return hyperDefinitions;
    }
}
