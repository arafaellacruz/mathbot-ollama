package com.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.logging.Logger;
import java.util.logging.Level;

public class TelegramBot extends TelegramLongPollingBot {
    
    private static final Logger LOGGER = Logger.getLogger(TelegramBot.class.getName());
    
    private final String botToken;
    private final String botUsername;
    private final OllamaClient ollamaClient;
    private final ChatMemory chatMemory;

    public TelegramBot(String botToken, String botUsername, OllamaClient ollamaClient) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.ollamaClient = ollamaClient;
        this.chatMemory = new ChatMemory(); 
        
        LOGGER.info("✅ TelegramBot initialized: " + botUsername);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        LOGGER.info("📨 Update received!");
        
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                Long chatId = update.getMessage().getChatId();
                
                LOGGER.info("💬 Message from chat " + chatId + ": " + messageText);
                
                if (messageText.startsWith("/")) {
                    handleCommand(chatId, messageText);
                } else {
                    handleUserMessage(chatId, messageText);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Error processing update", e);
        }
    }

    private void handleCommand(Long chatId, String command) {
        LOGGER.info("🔧 Command: " + command);
        
        try {
            // Normaliza o comando: remove o nome do bot (@...) e pega o primeiro token
            String normalizedCommand = command.toLowerCase().split(" ")[0].split("@")[0]; 

            switch (normalizedCommand) {
                case "/start":
                    sendMessage(chatId, 
                        "🤖 *Olá! Sou o MathBot*\n\n" +
                        "Envie sua pergunta de matemática!\n\n" +
                        "*Comandos:*\n" +
                        "/start - Iniciar\n" +
                        "/clear - Limpar histórico\n" +
                        "/test - Testar Ollama");
                    break;
                    
                case "/clear":
                    chatMemory.clearHistory(chatId);
                    sendMessage(chatId, "🧹 Histórico limpo!");
                    break;
                    
                case "/test":
                    testOllama(chatId);
                    break;
                    
                default:
                    sendMessage(chatId, "❓ Comando desconhecido. Use /start");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling command", e);
        }
    }

    private void handleUserMessage(Long chatId, String userMessage) {
        LOGGER.info("💭 Processing message...");
        
        try {
            sendTypingAction(chatId);
            
            // Pega o histórico ANTES de adicionar a mensagem atual
            var history = chatMemory.getHistory(chatId);
            
            // Adiciona a mensagem do usuário ao histórico ANTES da chamada ao Ollama
            chatMemory.addMessage(chatId, "user", userMessage); 
            
            LOGGER.info("🤖 Querying Ollama...");
            String response = ollamaClient.chat(history, userMessage);
            
            // Adiciona a resposta do assistente ao histórico
            chatMemory.addMessage(chatId, "assistant", response); 
            sendMessage(chatId, response);
            
            LOGGER.info("✅ Response sent");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling message", e);
            sendMessage(chatId, "❌ Erro: " + e.getMessage());
        }
    }

    private void testOllama(Long chatId) {
        sendMessage(chatId, "🔍 Testando Ollama...");
        
        if (ollamaClient.testConnection()) {
            sendMessage(chatId, "✅ Ollama conectado!\nModelo: " + ollamaClient.getModelName());
        } else {
            sendMessage(chatId, "❌ Ollama não está acessível.\nVerifique: ollama serve");
        }
    }

    private void sendMessage(Long chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);
            message.enableMarkdown(true);
            
            execute(message);
            LOGGER.info("📤 Message sent");
            
        } catch (TelegramApiException e) {
            LOGGER.log(Level.SEVERE, "Failed to send message", e);
        }
    }

    private void sendTypingAction(Long chatId) {
        try {
            SendChatAction action = new SendChatAction();
            action.setChatId(chatId.toString());
            action.setAction(ActionType.TYPING);
            execute(action);
        } catch (TelegramApiException e) {
            // Não é crítico
        }
    }
}