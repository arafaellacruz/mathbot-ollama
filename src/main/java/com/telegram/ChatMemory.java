package com.telegram;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatMemory {
    private final Map<Long, List<OllamaClient.Message>> byChat = new ConcurrentHashMap<>();
    private final int maxMessages = 20; // limita crescimento

    public List<OllamaClient.Message> getHistory(long chatId) {
        return byChat.computeIfAbsent(chatId, k -> new ArrayList<>());
    }

    public void addUserMessage(long chatId, String text) {
        List<OllamaClient.Message> history = getHistory(chatId);
        history.add(new OllamaClient.Message("user", text));
        trim(history);
    }

    public void addAssistantMessage(long chatId, String text) {
        List<OllamaClient.Message> history = getHistory(chatId);
        history.add(new OllamaClient.Message("assistant", text));
        trim(history);
    }

    public void reset(long chatId) {
        byChat.remove(chatId);
    }

    public void ensureSystemPrompt(long chatId, String prompt) {
        List<OllamaClient.Message> history = getHistory(chatId);
        if (history.stream().noneMatch(m -> "system".equals(m.role))) {
            history.add(0, new OllamaClient.Message("system", prompt));
        }
    }

    private void trim(List<OllamaClient.Message> history) {
        // preserva a primeira (system), remove as mais antigas do meio
        int max = maxMessages;
        if (history.size() > max) {
            // mantém índice 0 (system) e as últimas (recentes)
            List<OllamaClient.Message> keep = new ArrayList<>();
            keep.add(history.get(0));
            int start = Math.max(1, history.size() - (max - 1));
            keep.addAll(history.subList(start, history.size()));
            history.clear();
            history.addAll(keep);
        }
    }
}
