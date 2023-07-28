package org.ent.hyper;

import java.util.HashMap;
import java.util.Map;

public class FixedHyperManager extends HyperManager {

    private final Map<String, Object> fixed = new HashMap<>();

    @Override
    public void doFix(QualifiedKey qualifiedKey, Object value) {
        log.info("setting hyperparameter '{}'={}", qualifiedKey.get(), value);
        fixed.put(qualifiedKey.get(), value);
    }

    @Override
    public <T> T doGet(QualifiedKey qualifiedKey) {
        @SuppressWarnings("unchecked")
        T result = (T) fixed.get(qualifiedKey.get());
        if (result == null) {
            throw new IllegalStateException("Could not find requested hyperparameter '" + qualifiedKey.get()
                    + "', must be set beforehand");
        }
        return result;
    }
}
