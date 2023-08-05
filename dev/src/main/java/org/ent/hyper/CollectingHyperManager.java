package org.ent.hyper;

import java.util.ArrayList;
import java.util.List;

public class CollectingHyperManager extends HyperManager {

    private final List<HyperDefinition<?>> hyperDefinitions = new ArrayList<>();

    @Override
    protected <T> T doGet(HyperDefinition<T> hyperDefinitionResolved) {
        hyperDefinitions.add(hyperDefinitionResolved);
        return null;
    }

    @Override
    protected void doFix(QualifiedKey qualifiedKey, Object value, boolean override) {
        throw new UnsupportedOperationException();
    }

    public List<HyperDefinition<?>> getHyperDefinitions() {
        return hyperDefinitions;
    }
}
