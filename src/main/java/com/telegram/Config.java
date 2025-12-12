
package com.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

@JsonIgnoreProperties(ignoreUnknown = true) 
public class Config {

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    private String telegramToken;
    private String botUsername;
    private OllamaConfig ollama;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OllamaConfig {
        private String baseUrl;
        private String model;

        public OllamaConfig() {} 
        
        public String getBaseUrl() { return baseUrl; }
        public String getModel()    { return model; }

        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public void setModel(String model)     { this.model = model; }
    }

    public static Config load() throws Exception {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try (InputStream is = Config.class.getClassLoader().getResourceAsStream("bot-config.json")) {
            if (is != null) {
                LOGGER.info("📄 Carregando config do classpath: bot-config.json");
                Config config = mapper.readValue(is, Config.class);
                config.validate();
                return config;
            } else {
                LOGGER.info("ℹ️ bot-config.json não encontrado no classpath.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Erro lendo bot-config.json no classpath: " + e.getMessage(), e);
            throw e;
        }

        // 2) Caminho externo
        String configPath = System.getProperty("config");
        if (configPath != null && !configPath.isBlank()) {
            LOGGER.info("📄 Carregando config do arquivo: " + configPath);
            try {
                Config config = mapper.readValue(new File(configPath), Config.class);
                config.validate();
                return config;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "❌ Erro lendo arquivo de config: " + e.getMessage(), e);
                throw e;
            }
        }

        throw new IllegalStateException(
            "❌ Nenhuma configuração encontrada.\n" +
            "  • Coloque 'bot-config.json' em src/main/resources\n" +
            "  • ou rode com -Dconfig=\"C:\\caminho\\bot-config.json\""
        );
    }

    private void validate() {
        if (telegramToken == null || telegramToken.isBlank()) {
            throw new IllegalStateException("❌ 'telegramToken' não configurado no bot-config.json.");
        }
        if (botUsername == null || botUsername.isBlank()) {
            throw new IllegalStateException("❌ 'botUsername' não configurado no bot-config.json.");
        }
        if (ollama == null) {
            throw new IllegalStateException("❌ Seção 'ollama' não encontrada no bot-config.json.");
        }
        if (ollama.baseUrl == null || ollama.baseUrl.isBlank()) {
            throw new IllegalStateException("❌ 'ollama.baseUrl' não configurado no bot-config.json.");
        }
        if (ollama.model == null || ollama.model.isBlank()) {
            throw new IllegalStateException("❌ 'ollama.model' não configurado no bot-config.json.");
        }
    }

    public String getTelegramToken() { return telegramToken; }
    public String getBotUsername()   { return botUsername; }
    public String getOllamaBaseUrl() { return ollama.baseUrl; }
    public String getOllamaModel()   { return ollama.model; }

    public void setTelegramToken(String telegramToken) { this.telegramToken = telegramToken; }
    public void setBotUsername(String botUsername)     { this.botUsername = botUsername; }
    public void setOllama(OllamaConfig ollama)         { this.ollama = ollama; }
}
