package org.ent.hyper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class HyperManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    public <T> T get(HyperDefinition<T> hyperDefinition) {
        return doGet(resolve(hyperDefinition.getName()));
    }

    protected abstract <T> T doGet(QualifiedKey qualifiedKey);

    public <T> void fix(HyperDefinition<T> hyperDefinition, T value) {
        doFix(resolve(hyperDefinition.getName()), value);
    }

    public void fix(String hyperSelectionJson) {
        TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
        try {
            Map<String, Object> map = objectMapper.readValue(hyperSelectionJson, typeRef);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                QualifiedKey qualifiedKey = resolve(entry.getKey());
                doFix(qualifiedKey, entry.getValue());
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected abstract void doFix(QualifiedKey qualifiedKey, Object value);

    protected QualifiedKey resolve(String simpleKey) {
        // to be overridden
        return new QualifiedKey(simpleKey);
    }

    /**
     * Move to a subdirectory. Creates another view of the same HyperMangager
     * that interprets all property names as relative to the given subdirectory.
     * This allows to use the same property names in different contexts.
     */
    public HyperManager group(String group) {
        return new SubHyperManager(this, group);
    }
}
