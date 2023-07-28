package org.ent.hyper;

import java.util.ArrayList;
import java.util.List;

public class CollectingHyperManager extends HyperManager {

    private final List<HyperDefinition<?>> hyperDefinitions = new ArrayList<>();

    @Override
    public <T> T get(HyperDefinition<T> hyperDefinition) {
        hyperDefinitions.add(hyperDefinition.cloneWithName(resolve(hyperDefinition.getName()).get()));
        return null;
    }

    @Override
    protected <T> T doGet(QualifiedKey qualifiedKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doFix(QualifiedKey qualifiedKey, Object value) {
        throw new UnsupportedOperationException();
    }

    public List<HyperDefinition<?>> getHyperDefinitions() {
        return hyperDefinitions;
    }
}
