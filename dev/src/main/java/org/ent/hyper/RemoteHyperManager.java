package org.ent.hyper;

import com.fasterxml.jackson.core.JsonParser;
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

public class RemoteHyperManager extends FixedHyperManager {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }
    private static final OkHttpClient okHttpClient = new OkHttpClient();


    private final List<HyperDefinition<?>> hyperDefinitions;

    private Map<String, Object> suggested;
    private final Map<String, Object> fixed = new HashMap<>();


    public RemoteHyperManager(List<HyperDefinition<?>> hyperDefinitions) {
        this.hyperDefinitions = hyperDefinitions;
    }

    public Integer suggest() throws IOException {
        List<HyperDefinition<?>> toQuery = hyperDefinitions.stream().filter(hd -> !fixed.containsKey(hd.getName())).toList();
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
        SuggestResponse suggestResponse = objectMapper.readValue(responseBody, SuggestResponse.class);

        Map<String, Object> hps = suggestResponse.getParameters();
        this.suggested = hyperDefinitions.stream().collect(Collectors.toMap(HyperDefinition::getName, hd -> hps.get(hd.getName())));
        return suggestResponse.getTrial_number();
    }

    public void complete(Integer trialNumber, double value) throws IOException {
        if (trialNumber == null) {
            return;
        }
        CompleteRequest completeRequest = new CompleteRequest(trialNumber, value);
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

    @Override
    public <T> T doGet(QualifiedKey qualifiedKey) {
        Object result;
        String propertyName = qualifiedKey.get();
        Object fixedProp = this.fixed.get(propertyName);
        if (fixedProp != null) {
            result = fixedProp;
        } else {
            result = suggested.get(propertyName);
        }
        @SuppressWarnings("unchecked")
        T castedResult = (T) result;
        return castedResult;
    }

    public static class SuggestResponse {
        public int trial_number;
        public Map<String, Object> parameters;

        public int getTrial_number() {
            return trial_number;
        }

        public void setTrial_number(int trial_number) {
            this.trial_number = trial_number;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }
    }

    public static class CompleteRequest {
        private int trial_number;

        private double value;

        public CompleteRequest(int trial_number, double value) {
            this.trial_number = trial_number;
            this.value = value;
        }

        public int getTrial_number() {
            return trial_number;
        }

        public void setTrial_number(int trial_number) {
            this.trial_number = trial_number;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }
}
