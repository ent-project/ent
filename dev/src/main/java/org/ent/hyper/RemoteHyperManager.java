package org.ent.hyper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RemoteHyperManager extends HyperManager {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }
    private static final OkHttpClient okHttpClient = new OkHttpClient();


    private final List<HyperDefinition> hyperDefinitions;

    private Map<String, Object> suggested;
    private Map<String, Object> fixed = new HashMap<>();


    public RemoteHyperManager(List<HyperDefinition> hyperDefinitions) {
        this.hyperDefinitions = hyperDefinitions;
    }

    public Integer suggest() throws IOException {
        List<HyperDefinition> toQuery = hyperDefinitions.stream().filter(hd -> !fixed.containsKey(hd.getName())).toList();
        if (toQuery.isEmpty()) {
            return null;
        }
        String jsonInputString = objectMapper.writeValueAsString(hyperDefinitions);
        RequestBody requestBody = RequestBody.create(
                jsonInputString,
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("http://localhost:5005/suggest")
                .post(requestBody)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        String responseBody = response.body().string();
        System.err.println(responseBody);
        HpoService.SuggestResponse suggestResponse = objectMapper.readValue(responseBody, HpoService.SuggestResponse.class);

        Map<String, Object> hps = suggestResponse.getParameters();
        this.suggested = hyperDefinitions.stream().collect(Collectors.toMap(HyperDefinition::getName, hd -> hps.get(hd.getName())));
        return suggestResponse.getTrial_number();
    }

    public void complete(Integer trialNumber, double value) throws IOException {
        if (trialNumber == null) {
            return;
        }
        HpoService.CompleteRequest completeRequest = new HpoService.CompleteRequest(trialNumber, value);
        ObjectMapper objectMapper = new ObjectMapper();
        String completeRequestJson = objectMapper.writeValueAsString(completeRequest);

        RequestBody requestBody = RequestBody.create(
                completeRequestJson,
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("http://localhost:5005/complete")
                .post(requestBody)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            System.err.println("complete response: " + response);
        }
    }

    public Object getProp(String propertyName) {
        Object fixedProp = this.fixed.get(propertyName);
        if (fixedProp != null) {
            return fixedProp;
        } else {
            return suggested.get(propertyName);
        }
    }

    @Override
    public double get(DoubleHyperDefinition hyperDefinition) {
        return (double) getProp(hyperDefinition.getName());
    }

    @Override
    public int get(IntHyperDefinition hyperDefinition) {
        return (int) getProp(hyperDefinition.getName());
    }


    public void fixParameters(String hyperSelectionJson) {
        TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
        try {
            this.fixed.putAll(objectMapper.readValue(hyperSelectionJson, typeRef));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
