package ru.florestdev.interactivePets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AIConnect {
    private  InteractivePets plugin;
    private String key;
    private String system_prompt;
    private String ai_service;
    private String model;
    private final Gson gson = new Gson();
    private final List<JsonObject> chatHistory = new ArrayList<>();
    private static final int MAX_MEMORY = 500;
    private final HttpClient http = HttpClient.newHttpClient();
    public AIConnect(InteractivePets plugin, String key, String system_prompt, String ai_service, String model) {
        this.plugin = plugin;
        this.key = key;
        this.system_prompt = system_prompt;
        this.ai_service = ai_service;
        this.model = model;
    }

    private void updateMemory(String user, String assistant) {
        chatHistory.add(createMsg("user", user));
        chatHistory.add(createMsg("assistant", assistant));
        if (chatHistory.size() > MAX_MEMORY) { chatHistory.remove(0); chatHistory.remove(0); }
    }

    private JsonObject createMsg(String role, String content) {
        JsonObject m = new JsonObject();
        m.addProperty("role", role);
        m.addProperty("content", content);
        return m;
    }

    public CompletableFuture<String> processAI(String action) {


        JsonObject body = new JsonObject();
        body.addProperty("model", model);

        JsonArray messages = new JsonArray();
        String sysPrompt = system_prompt;

        messages.add(createMsg("system", sysPrompt));
        for (JsonObject oldMsg : chatHistory) messages.add(oldMsg);
        messages.add(createMsg("user", action));
        body.add("messages", messages);

        String url = ai_service;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)));

        builder.header("Authorization", "Bearer " + this.key);

        return http.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    try {
                        JsonObject resObj = gson.fromJson(resp.body(), JsonObject.class);
                        String content = resObj.getAsJsonObject("message").get("content").getAsString();
                                resObj.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
                        updateMemory(action, content);
                        return content.trim();
                    } catch (Exception e) {}
                    return null;
                });
    }



}
