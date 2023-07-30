package org.ent.hyper;

import java.util.HashMap;
import java.util.Map;

public class FixedHyperManager extends HyperManager {

    protected final Map<String, Object> fixed = new HashMap<>();

    @Override
    public void doFix(QualifiedKey qualifiedKey, Object value) {
        log.info("setting hyperparameter '{}'={}", qualifiedKey.get(), value);
        fixed.put(qualifiedKey.get(), value);
    }

    @Override
    public <T> T doGet(HyperDefinition<T> hyperDefinitionResolved) {
        String resolvedName = hyperDefinitionResolved.getName();
        @SuppressWarnings("unchecked")
        T result = (T) fixed.get(resolvedName);
        if (result == null) {
            throw new IllegalStateException("Could not find requested hyperparameter '" + resolvedName
                                            + "', must be set beforehand");
        }
        return result;
    }
}
