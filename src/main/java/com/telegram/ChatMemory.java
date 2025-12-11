package com.telegram;

import java.util.ArrayList;
import java.util.Collections; // Importação adicionada
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ChatMemory {
    
    private static final Logger LOGGER = Logger.getLogger(ChatMemory.class.getName());
    // Define o limite máximo de mensagens no histórico (5 pares user/assistant)
    private static final int MAX_HISTORY_SIZE = 10; 
    
    // Usa ConcurrentHashMap para ser thread-safe
    private final Map<Long, List<Map<String, String>>> conversations;

    public ChatMemory() {
        this.conversations = new ConcurrentHashMap<>();
        LOGGER.info("✅ ChatMemory initialized");
    }

    /**
     * Retorna o histórico de mensagens para um chatId específico.
     * Retorna uma lista vazia e imutável se não houver histórico,
     * para evitar NullPointerExceptions ou modificações indesejadas.
     * @param chatId O ID do chat.
     * @return Lista de mensagens (Map com "role" e "content").
     */
    public List<Map<String, String>> getHistory(Long chatId) {
        // Usa getOrDefault para retornar uma lista vazia se o chat não existir,
        // garantindo que o OllamaClient receba algo válido.
        return conversations.getOrDefault(chatId, Collections.emptyList());
    }

    /**
     * Adiciona uma nova mensagem ao histórico do chat e aplica o limite de tamanho.
     * @param chatId O ID do chat.
     * @param role O papel na conversa ("user" ou "assistant").
     * @param content O conteúdo da mensagem.
     */
    public void addMessage(Long chatId, String role, String content) {
        // Usa computeIfAbsent para obter a lista ou criar uma nova se não existir,
        // garantindo thread-safety. (Um ArrayList normal já funciona bem com ConcurrentHashMap).
        List<Map<String, String>> history = conversations.computeIfAbsent(
            chatId, 
            k -> new ArrayList<>()
        );
        
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        
        history.add(message);
        
        // Mantém o histórico com um tamanho máximo, removendo mensagens mais antigas (FIFO).
        if (history.size() > MAX_HISTORY_SIZE) {
            // Remove a mensagem mais antiga (índice 0)
            history.remove(0);
        }
    }

    /**
     * Limpa o histórico de conversas para um chat específico.
     * @param chatId O ID do chat.
     */
    public void clearHistory(Long chatId) {
        conversations.remove(chatId);
        LOGGER.info("🧹 Cleared history for chat: " + chatId);
    }
}