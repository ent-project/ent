package org.ent.hyper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ent.dev.game.forwardarithmetic.StageReadInfo2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class FixedHyperManager extends HyperManager {

    private static final Logger log = LoggerFactory.getLogger(StageReadInfo2.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    private final Map<String, Object> fixed = new HashMap<>();

    public void setParameters(String hyperSelectionJson) {
        setParameters(hyperSelectionJson, PropertyNameResolver.IDENTITY_RESOLVER);
    }

    public void setParameters(String hyperSelectionJson, PropertyNameResolver resolver) {
        TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
        try {
            Map<String, Object> map = objectMapper.readValue(hyperSelectionJson, typeRef);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String name = resolver.resolve(entry.getKey());
                log.info("setting hyperparameter '{}'={}", name, entry.getValue());
                this.fixed.put(name, entry.getValue());
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void setParameter(DoubleHyperDefinition hyper, double value) {
        doSetParameter(hyper, value);
    }

    public void setParameter(IntHyperDefinition hyper, int value) {
        doSetParameter(hyper, value);
    }

    public void setParameter(DoubleHyperDefinition hyper, Object value, PropertyNameResolver resolver) {
        doSetParameter(hyper, value, resolver);
    }

    public void setParameter(IntHyperDefinition hyper, Object value, PropertyNameResolver resolver) {
        doSetParameter(hyper, value, resolver);
    }

    private void doSetParameter(HyperDefinition hyper, Object value) {
        doSetParameter(hyper, value, PropertyNameResolver.IDENTITY_RESOLVER);
    }

    private void doSetParameter(HyperDefinition hyper, Object value, PropertyNameResolver resolver) {
        String name = resolver.resolve(hyper.getName());
        log.info("setting hyperparameter '{}'={}", name, value);
        fixed.put(name, value);
    }

    @Override
    public <T> T get(NumericHyperDefinition<T> hyperDefinition) {
        @SuppressWarnings("unchecked")
        T result = (T) fixed.get(hyperDefinition.getName());
        if (result == null) {
            throw new IllegalStateException("Could not find requested hyperparameter '" + hyperDefinition.getName()
                    + "', must be set beforehand");
        }
        return result;
    }
}
