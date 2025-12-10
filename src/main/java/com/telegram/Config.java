package com.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public String telegramToken;
    public String botUsername;
    public Ollama ollama = new Ollama();

    public static class Ollama {
        public String baseUrl = "http://localhost:11434";
        public String model = "mistral";
    }

    /**
     * Tenta carregar do classpath (resources). Se não encontrar, tenta por caminho de arquivo.
     */
    public static Config load(String nameOrPath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 1) Tenta classpath (resources)
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream(nameOrPath)) {
            if (is != null) {
                return mapper.readValue(is, Config.class);
            }
        }

        // 2) Tenta caminho de arquivo (externo)
        try (InputStream is = Files.newInputStream(Path.of(nameOrPath))) {
            return mapper.readValue(is, Config.class);
        }
    }
}
