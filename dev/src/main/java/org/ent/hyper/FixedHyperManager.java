package org.ent.hyper;

import java.util.HashMap;
import java.util.Map;

public class FixedHyperManager extends HyperManager {

    protected final Map<String, Object> fixed = new HashMap<>();

    @Override
    public void doFix(QualifiedKey qualifiedKey, Object value, boolean override) {
        String key = qualifiedKey.get();
        log.info("setting hyperparameter '{}'={}", key, value);
        if (override) {
            if (!fixed.containsKey(key)) {
                throw new IllegalStateException(
                        "Trying to override key '%s' with value %s, but was not set before".formatted(key, value));
            }
            if (value == null) {
                fixed.remove(key);
            } else {
                fixed.put(key, value);
            }
        } else {
            if (fixed.containsKey(key)) {
                throw new IllegalStateException("Key '%s' is already set".formatted(key));
            }
            fixed.put(key, value);
        }
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
