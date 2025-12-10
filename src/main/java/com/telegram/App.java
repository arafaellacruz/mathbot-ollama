package com.telegram;
 

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class App {
    public static void main(String[] args) throws Exception {
        // Permite trocar o arquivo por VM option: -Dconfig=... (opcional)
        String cfgName = System.getProperty("config", "bot-config.json");

        Config cfg = Config.load(cfgName);

        if (cfg.telegramToken == null || cfg.telegramToken.isBlank()
                || cfg.botUsername == null || cfg.botUsername.isBlank()) {
            throw new IllegalStateException("Configure telegramToken e botUsername em " + cfgName +
                    " (classpath ou caminho externo via -Dconfig=...)");
        }

        // Cria clientes com base no que veio do config
        OllamaClient ollama = new OllamaClient(cfg.ollama.baseUrl, cfg.ollama.model);
        ChatMemory memory = new ChatMemory();

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new TelegramBot(cfg.telegramToken, cfg.botUsername, ollama, memory));

        System.out.printf("Bot iniciado como @%s | Modelo: %s | Ollama: %s%n",
                cfg.botUsername, cfg.ollama.model, cfg.ollama.baseUrl);
    }
}
