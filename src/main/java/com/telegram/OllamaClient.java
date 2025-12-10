package com.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
    
public class OllamaClient {
    private final String baseUrl;
    private final String model;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public OllamaClient(String baseUrl, String model) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public String chat(List<Message> messages) throws Exception {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("model", model);
        payload.put("stream", false);

        ArrayNode msgs = mapper.createArrayNode();
        for (Message m : messages) {
            ObjectNode node = mapper.createObjectNode();
            node.put("role", m.role);
            node.put("content", m.content);
            msgs.add(node);
        }
        payload.set("messages", msgs);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Erro Ollama: HTTP " + resp.statusCode() + " - " + resp.body());
        }

        JsonNode root = mapper.readTree(resp.body());
        JsonNode message = root.get("message");
        if (message == null || message.get("content") == null) {
            return "(sem conteúdo)";
        }
        return message.get("content").asText();
    }

    // DTO simples
    public static class Message {
        public final String role;   // "system" | "user" | "assistant"
        public final String content;
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
