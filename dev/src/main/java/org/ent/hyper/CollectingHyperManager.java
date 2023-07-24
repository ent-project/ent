package org.ent.hyper;

import java.util.ArrayList;
import java.util.List;

public class CollectingHyperManager extends HyperManager {

    private final List<HyperDefinition> hyperDefinitions = new ArrayList<>();

    @Override
    public <T> T get(NumericHyperDefinition<T> hyperDefinition) {
        hyperDefinitions.add(hyperDefinition);
        return hyperDefinition.getAverageValue();
    }

    public List<HyperDefinition> getHyperDefinitions() {
        return hyperDefinitions;
    }
}
