# 🤖 MathBot — Chatbot de Matemática em Java + Ollama + Telegram

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3+-brightgreen.svg)](https://maven.apache.org/)
[![Telegram](https://img.shields.io/badge/Telegram-Bot-blue.svg)](https://core.telegram.org/bots)
[![Ollama](https://img.shields.io/badge/Ollama-Local%20LLM-9cf.svg)](https://ollama.com/)

Um chatbot simples que responde **perguntas de matemática básica** (com explicações passo a passo) usando:
- **Java puro** (sem Spring)
- **Ollama** como LLM local
- **Telegram** como interface com o usuário

> **Objetivo**: didático, minimalista e ideal para apresentações e protótipos locais.

---

## 🧠 Arquitetura

```
Usuário (Telegram)
       │
       ▼
Telegram Bot (Java, Long Polling)
       │
       ▼
Ollama (LLM local via HTTP /api/chat)
       │
       ▼
Resposta no Telegram
```

- **Long Polling**: simples, sem precisar expor servidor público.
- **Ollama**: os dados **não saem da sua máquina** (privacidade e controle total).

---

## 📦 Estrutura do Projeto

```
src/
 └─ main/
     ├─ java/com/rafaella/bot/
     │   ├─ Main.java            # Inicializa o bot
     │   ├─ TelegramBot.java     # Integra com Telegram
     │   ├─ OllamaClient.java    # HTTP client p/ Ollama (/api/chat)
     │   ├─ ChatMemory.java      # Contexto por chat (histórico + system prompt)
     │   └─ Config.java          # Leitura de bot-config.json/.properties
     └─ resources/
         ├─ bot-config.json      # Configurações (token, username, modelo) [não versionar]
         └─ bot-config.sample.json  # Exemplo com placeholders [versionar]
```

---

## ✅ Pré-requisitos

- **Java 17+** (JDK)
- **Maven 3+**
- **Ollama** instalado e rodando:
  ```bash
  ollama serve
  # modelos sugeridos:
  ollama pull mistral
  # ou, melhor qualidade:
  ollama pull llama3.1:8b
  ```

- **Bot no Telegram**:
  1. Fale com **@BotFather**, envie `/newbot`
  2. Defina **nome** e **username** (ex.: `MathBot`)
  3. Guarde o **token** (ex.: `123456789:ABC-xyz...`)

---

## ⚙️ Configuração

Há **três formas** de configurar o token/nome do bot e o modelo do Ollama:

### 1) Arquivo JSON (recomendado)
`src/main/resources/bot-config.json`:
```json
{
  "telegramToken": "SEU_TOKEN_AQUI",
  "botUsername": "MathBot",
  "ollama": {
    "baseUrl": "http://localhost:11434",
    "model": "mistral"
  }
}
```

> **Boas práticas**:
> - Versione **apenas** `bot-config.sample.json` com placeholders.
> - Adicione `bot-config.json` ao `.gitignore`.

### 2) Arquivo `.properties` (alternativa simples)
`src/main/resources/bot-config.properties`:
```properties
telegram.token=SEU_TOKEN_AQUI
telegram.username=MathBot
ollama.baseUrl=http://localhost:11434
ollama.model=mistral
```

### 3) Variáveis de ambiente (opcional)
```bash
# Linux/macOS
export TELEGRAM_BOT_TOKEN="SEU_TOKEN"
export TELEGRAM_BOT_USERNAME="MathBot"
export OLLAMA_MODEL="mistral"
```
```powershell
# Windows PowerShell
$env:TELEGRAM_BOT_TOKEN="SEU_TOKEN"
$env:TELEGRAM_BOT_USERNAME="MathBot"
$env:OLLAMA_MODEL="mistral"
```

> Você pode **sobrepor** o arquivo de config com `-Dconfig`:
```bash
java -Dconfig="/caminho/para/bot-config.json" -jar target/java-telegram-ollama-mathbot-1.0.0.jar
```

---

## 🚀 Build & Run

### Maven
```bash
mvn clean package
```

### Executar (Linux/WSL/macOS)
```bash
java -jar target/java-telegram-ollama-mathbot-1.0.0.jar
```

### Executar (Windows CMD/PowerShell)
```powershell
java -jar target\java-telegram-ollama-mathbot-1.0.0.jar
```

> **WSL dica**: use `/` (barra normal). `\` é escape no Linux:
```bash
java -jar target/java-telegram-ollama-mathbot-1.0.0.jar
```

---

## 💬 Como usar

No Telegram, fale com seu bot (ex.: `@MathBot`):

**Comandos:**
- `/start` — mensagem de boas-vindas e início de sessão
- `/clear` — limpa o contexto da conversa

**Exemplos de perguntas:**
- `2 + 2`
- `Quanto é 15% de 200?`
- `Resolva: 3x + 6 = 0`
- `Como transformar 0,75 em fração?`

O bot responde com **explicações passo a passo** (system prompt ajustado para matemática básica).

---

## 🧩 Como funciona (resumo técnico)

1. `TelegramBot` recebe a mensagem via **Long Polling**
2. `ChatMemory` mantém **system prompt** e histórico por `chatId`
3. `OllamaClient` faz POST em `http://localhost:11434/api/chat` com:
   ```json
   {
     "model": "mistral",
     "messages": [
       {"role": "system", "content": "...tutor de matemática..."},
       {"role": "user", "content": "Pergunta do usuário"}
     ],
     "stream": false
   }
   ```
4. A resposta volta e é enviada ao usuário no Telegram

---

## 🧱 Dependências (por que precisamos delas)

- **`org.telegram:telegrambots`**  
  Integração fácil com Telegram via **Long Polling** (recebe/manda mensagens).
- **`com.fasterxml.jackson.core:jackson-databind`**  
  Serializa/desserializa **JSON** para conversar com o **Ollama**.

> Projeto em **Java puro** para **clareza** e **simplicidade** na apresentação.

---

## 🛠️ Troubleshooting

- **`NoSuchFileException: bot-config.json`**  
  → O arquivo não foi encontrado. Soluções:
  - Coloque em `src/main/resources` e rode `mvn package`.
  - Ou passe `-Dconfig="C:\\caminho\\bot-config.json"` (no Windows).
  - Verifique o diretório atual com `pwd` (WSL) ou `cd`.

- **`Unable to access jarfile targetjava-...`** (WSL/Linux)  
  → Usou `\\` no caminho. Use `/`:
  ```bash
  java -jar target/java-telegram-ollama-mathbot-1.0.0.jar
  ```

- **Bot não responde / TOKEN inválido**  
  → Confira o token no BotFather. Verifique internet do PC e se o bot não está **bloqueado** no Telegram.

- **Ollama não responde**  
  → Garanta que está rodando: `ollama serve`.  
    Teste: `curl http://localhost:11434/api/tags`

---

## 🔒 Segurança

- **Não** commite o **token** do Telegram.  
- Use `bot-config.sample.json` no repositório + `bot-config.json` real no `.gitignore`.
- O **LLM é local** (Ollama) → dados ficam na sua máquina.

---

## 🗺️ Roadmap (opcional)

- ✅ Respostas passo a passo (matemática básica)  
- ⏳ **Streaming** de tokens (`stream: true`)  
- ⏳ **Fallback determinístico** para expressões simples (ex.: `2+2`, `3*7`)  
- ⏳ Persistir memória em arquivo/BD  
- ⏳ Comandos: `/mode` (Matemática, Programação), `/about`  
- ⏳ Formatação em **Markdown** para explicações mais bonitas

---

## 🤝 Contribuição

1. Faça um fork  
2. Crie sua branch: `git checkout -b feature/minha-ideia`  
3. Commit: `git commit -m "feat: minha ideia"`  
4. Push: `git push origin feature/minha-ideia`  
5. Abra um PR

---

## 👩🏽‍💻 Créditos

Projeto criado por **Rafaella Cruz** para demonstrar um chatbot de matemática simples com Java + Ollama + Telegram, com foco em didática e simplicidade.
