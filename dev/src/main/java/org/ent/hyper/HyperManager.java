package org.ent.hyper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class HyperManager {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .build();

    public <T> T get(HyperDefinition<T> hyperDefinition) {
        return doGet(hyperDefinition.cloneWithName(resolve(hyperDefinition.getName()).get()));
    }

    protected abstract <T> T doGet(HyperDefinition<T> hyperDefinitionResolved);

    public <T> void fix(HyperDefinition<T> hyperDefinition, T value) {
        doFix(resolve(hyperDefinition.getName()), value, false);
    }

    public void fixJson(String hyperSelectionJson) {
        TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {
        };
        try {
            Map<String, Object> map = objectMapper.readValue(hyperSelectionJson, typeRef);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                QualifiedKey qualifiedKey = resolve(entry.getKey());
                doFix(qualifiedKey, entry.getValue(), false);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void fixLines(String linesData) {
        fixLines(linesData, false);
    }

    public void overrideLines(String linesData) {
        fixLines(linesData, true);
    }

    private void fixLines(String linesData, boolean override) {
        linesData.lines()
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .filter(s -> !s.startsWith("//"))
                .forEach(p ->
                {
                    String[] s = p.split(" ");
                    if (s.length != 2) {
                        throw new IllegalArgumentException();
                    }
                    QualifiedKey qualifiedKey = resolve(s[0]);
                    try {
                        Object value = objectMapper.readValue(s[1], Object.class);
                        doFix(qualifiedKey, value, override);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Provide/fix a value for a hyperparameter that will be returned for subsequent queries.
     *
     * @param override if true, replaces a value that has already been fixed before, will throw an exception in case it
     *                is not already present
     *                 if false, validates that it has not already been fixed before
     */
    protected abstract void doFix(QualifiedKey qualifiedKey, Object value, boolean override);

    /**
     * Move to a subdirectory. Creates another view of the same HyperManager
     * that interprets all property names as relative to the given subdirectory.
     * This allows to use the same HyperDefinition instances in different contexts.
     * (E.g. 2 instances of the same random generator class are used in a process, but with
     * different set of hyperparameters.)
     */
    public HyperManager group(String group) {
        return new SubHyperManager(this, group);
    }

    protected QualifiedKey resolve(String simpleKey) {
        // to be overridden
        return new QualifiedKey(simpleKey);
    }
}
