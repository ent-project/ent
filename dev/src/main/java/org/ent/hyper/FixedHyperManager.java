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

    private Map<String, Object> fixed = new HashMap<>();

    public void setParameters(String hyperSelectionJson) {
        TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
        try {
            Map<String, Object> map = objectMapper.readValue(hyperSelectionJson, typeRef);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                log.info("setting hyperparameter {}={}", entry.getKey(), entry.getValue());
                this.fixed.put(entry.getKey(), entry.getValue());
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void setParameter(HyperDefinition hyper, Object value) {
        log.info("setting hyperparameter {}={}", hyper.getName(), value);
        fixed.put(hyper.getName(), value);
    }

    @Override
    public double get(DoubleHyperDefinition hyperDefinition) {
        return (double) fixed.get(hyperDefinition.getName());
    }

    @Override
    public int get(IntHyperDefinition hyperDefinition) {
        return (int) fixed.get(hyperDefinition.getName());
    }
}
