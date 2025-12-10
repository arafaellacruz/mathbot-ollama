package com.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class App {
    public static void main(String[] args) throws Exception {
        // Permite override por linha de comando: -Dconfig=meu-arquivo.json
        String cfgName = System.getProperty("config", "bot-config.json");

        Config cfg = Config.load(cfgName);
        if (cfg.telegramToken == null || cfg.botUsername == null) {
            throw new IllegalStateException("Defina telegramToken e botUsername em " + cfgName);
        }

        OllamaClient ollama = new OllamaClient(cfg.ollama.baseUrl, cfg.ollama.model);
        ChatMemory memory = new ChatMemory();

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new TelegramBot(cfg.telegramToken, cfg.botUsername, ollama, memory));

        System.out.println("Bot iniciado como @" + cfg.botUsername + " usando modelo " + cfg.ollama.model);
    }
}
