package com.telegram;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;


public class Config {

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    private String telegramToken;
    private String botUsername;
    private OllamaConfig ollama;

    // Classe interna para mapear a seção aninhada "ollama" no JSON
    public static class OllamaConfig {
        private String baseUrl;
        private String model;

        public OllamaConfig() {} // Construtor obrigatório para Jackson

        public String getBaseUrl() { return baseUrl; }
        public String getModel() { return model; }
    }


    public static Config load() throws Exception {

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 1. Tenta carregar do recurso (bot-config.json)
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream("bot-config.json")) {
            if (is != null) {
                LOGGER.info("📄 Loading config from bot-config.json");
                Config config = mapper.readValue(is, Config.class);
                config.validate();
                return config;
            }
        } catch (Exception e) {
            LOGGER.warning("Could not load bot-config.json: " + e.getMessage());
        }

        // 2. Tenta carregar de um arquivo externo
        String configPath = System.getProperty("config");
        if (configPath != null) {
            LOGGER.info("📄 Loading config from: " + configPath);
            Config config = mapper.readValue(new File(configPath), Config.class);
            config.validate();
            return config;
        }

        // 3. Carrega de Environment Variables
        LOGGER.info("📄 Loading config from environment variables (with defaults)");
        Config config = new Config();
        // Use valores Padrão se as variáveis de ambiente não estiverem definidas
        config.telegramToken = System.getenv().getOrDefault("TELEGRAM_BOT_TOKEN", "TOKEN_PADRAO_PARA_TESTE");
        config.botUsername = System.getenv().getOrDefault("TELEGRAM_BOT_USERNAME", "@USUARIO_PADRAO");

        config.ollama = new OllamaConfig();
        config.ollama.baseUrl = System.getenv().getOrDefault("OLLAMA_BASE_URL", "http://localhost:11434");
        config.ollama.model = System.getenv().getOrDefault("OLLAMA_MODEL", "mistral");

        config.validate(); // Isso agora deve passar
        return config;
    }

    private void validate() {
        if (telegramToken == null || telegramToken.isBlank()) {
            throw new IllegalStateException("❌ Telegram token not configured!");
        }
        if (botUsername == null || botUsername.isBlank()) {
            throw new IllegalStateException("❌ Bot username not configured!");
        }
        if (ollama == null) {
            throw new IllegalStateException("❌ Ollama config not found!");
        }
    }

    // Getters
    public String getTelegramToken() { return telegramToken; }
    public String getBotUsername() { return botUsername; }
    public String getOllamaBaseUrl() { return ollama.baseUrl; }
    public String getOllamaModel() { return ollama.model; }
}