package com.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramBot extends TelegramLongPollingBot {
    private final String token;
    private final String username;
    private final OllamaClient ollama;
    private final ChatMemory memory;

    public TelegramBot(String token, String username, OllamaClient ollama, ChatMemory memory) {
        super(token);
        this.token = token;
        this.username = username;
        this.ollama = ollama;
        this.memory = memory;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();

        // Comandos básicos
        if (text.equalsIgnoreCase("/start")) {
            send(chatId, "Olá! Sou o RafaMath 🤖. Faça perguntas simples de matemática.\n" +
                    "Ex.: 2+2, frações, porcentagens, equações básicas.\n" +
                    "Comandos: /clear para limpar o contexto.");
            memory.reset(chatId);
            return;
        }
        if (text.equalsIgnoreCase("/clear")) {
            memory.reset(chatId);
            send(chatId, "Contexto limpo. Vamos recomeçar!");
            return;
        }

        // System prompt especializado para matemática simples
        String systemPrompt = """
            Você é um tutor de matemática. Responda de forma clara e passo a passo.
            Regras:
            - Mostre a conta e o raciocínio em etapas.
            - Use notação simples (frações, porcentagens, potência).
            - Se a pergunta for ambígua, peça esclarecimentos antes de responder.
            - Seja direto e didático.
            """;
        memory.ensureSystemPrompt(chatId, systemPrompt);

        // Adiciona a mensagem do usuário ao histórico
        memory.addUserMessage(chatId, text);

        try {
            // Chama o modelo com todo o contexto
            String answer = ollama.chat(memory.getHistory(chatId));
            memory.addAssistantMessage(chatId, answer);
            send(chatId, answer);
        } catch (Exception e) {
            e.printStackTrace();
            send(chatId, "Desculpe, ocorreu um erro ao consultar o modelo. Tente novamente.");
        }
    }

    private void send(long chatId, String text) {
        try {
            SendMessage msg = SendMessage.builder()
                    .chatId(Long.toString(chatId))
                    .text(text)
                    .build();
            execute(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

