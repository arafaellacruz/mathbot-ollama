package com.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            // 1. Carrega a configuração (Config.java)
            Config config = Config.load();

            String token = config.getTelegramToken();
            String username = config.getBotUsername();
            String ollamaUrl = config.getOllamaBaseUrl();
            String ollamaModel = config.getOllamaModel();
            
            // 2. Cria o cliente Ollama (OllamaClient.java)
            OllamaClient ollamaClient = new OllamaClient(ollamaUrl, ollamaModel);
            
            // Verifica a conexão com o Ollama antes de iniciar o bot
            if (!ollamaClient.testConnection()) {
                LOGGER.severe("🚨 Não foi possível conectar ao Ollama. O bot será iniciado, mas não funcionará.");
                // Continua, mas o erro será tratado pelo TelegramBot/OllamaClient
            } else {
                LOGGER.info("🎉 Conexão com Ollama bem-sucedida!");
            }

            // 3. Inicializa o API do Telegram Bots
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // 4. Registra o Bot (TelegramBot.java)
            TelegramBot bot = new TelegramBot(token, username, ollamaClient);
            botsApi.registerBot(bot);
            
            LOGGER.info("🚀 Telegram Bot iniciado com sucesso! (@" + username + ")");

        } catch (TelegramApiException e) {
            LOGGER.log(Level.SEVERE, "❌ Erro ao registrar o Telegram Bot na API", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Erro fatal na inicialização da aplicação", e);
        }
    }
}