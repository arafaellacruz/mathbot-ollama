package com.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class OllamaClient {
    
    private static final Logger LOGGER = Logger.getLogger(OllamaClient.class.getName());
    
    private final String baseUrl;
    private final String modelName;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;

    public OllamaClient(String baseUrl, String modelName) {
        this.baseUrl = baseUrl;
        this.modelName = modelName;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        
        this.systemPrompt = 
            "Você é um Tutor de Matemática Inteligente para o Telegram. " +
            "Responda em PT-BR\n" +
            "Sem LaTeX\n" +
            "Seja conciso e direto nas respostas, sem dar muitos outros contextos.";
        
        LOGGER.info("✅ OllamaClient initialized - URL: " + baseUrl + ", Model: " + modelName);
    }

    public String chat(List<Map<String, String>> history, String userMessage) {
        try {
            ArrayNode messages = objectMapper.createArrayNode();
            
            // Add system prompt
            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
            
            // Add history
            for (Map<String, String> msg : history) {
                ObjectNode historyMsg = objectMapper.createObjectNode();
                historyMsg.put("role", msg.get("role"));
                historyMsg.put("content", msg.get("content"));
                messages.add(historyMsg);
            }
            
            // Add current message
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            // Build request
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", modelName);
            requestBody.set("messages", messages);
            requestBody.put("stream", false);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            LOGGER.info("📤 Sending request to Ollama...");
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/chat"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(120))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(
                request, 
                HttpResponse.BodyHandlers.ofString()
            );
            
            LOGGER.info("📥 Response status: " + response.statusCode());
            
            if (response.statusCode() == 200) {
                var jsonResponse = objectMapper.readTree(response.body());
                String answer = jsonResponse.get("message").get("content").asText();
                LOGGER.info("✅ Got response from Ollama");
                return answer;
            } else {
                String error = "Erro do Ollama: " + response.statusCode();
                LOGGER.severe(error);
                return error;
            }
            
        } catch (java.net.ConnectException e) {
            String error = "❌ Não foi possível conectar ao Ollama.\nVerifique se está rodando: ollama serve";
            LOGGER.log(Level.SEVERE, error, e);
            return error;
            
        } catch (Exception e) {
            String error = "❌ Erro: " + e.getMessage();
            LOGGER.log(Level.SEVERE, error, e);
            return error;
        }
    }

    public boolean testConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/tags"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            
            return response.statusCode() == 200;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Cannot connect to Ollama", e);
            return false;
        }
    }

    public String getModelName() {
        return modelName;
    }
}