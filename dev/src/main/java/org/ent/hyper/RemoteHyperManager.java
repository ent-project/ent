package org.ent.hyper;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RemoteHyperManager extends HyperManager {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final OkHttpClient okHttpClient = new OkHttpClient();


    private final List<HyperDefinition> hyperDefinitions;

    private Map<String, Object> suggested;

    public RemoteHyperManager(List<HyperDefinition> hyperDefinitions) {
        this.hyperDefinitions = hyperDefinitions;
    }

    public int suggest() throws IOException {
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

    public void complete(int trialNumber, double value) throws IOException {
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

    @Override
    public double getDouble(String propertyName, double minValue, double maxValue) {
        return (double) suggested.get(propertyName);
    }

    @Override
    public int getInt(String propertyName, int minValue, int maxValue) {
        return (int) suggested.get(propertyName);
    }
}
