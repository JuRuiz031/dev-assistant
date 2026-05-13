# CLI Dev Assistant — Alpha

A lightweight agentic developer assistant running entirely on your local machine. Powered by Spring AI and Ollama — it reads your actual project files, navigates your codebase, and answers questions grounded in reality rather than training data.

Built for Ollama out of the box. Other model providers need dependency and config changes.

---

## What it can do

- List and navigate your project structure
- Read source code, config files, and READMEs
- Search for files by name or pattern
- Read git history, status, and diffs
- Remember your full conversation context
- Stream responses token by token

---

## Prerequisites

- Java 21
- Python 3
- Ollama — https://ollama.com/download

**Install Java 21:**

Windows: download from https://adoptium.net and run the installer.

Linux:
```bash
sudo apt install openjdk-21-jdk
```

---

## Setup

```bash
git clone https://github.com/JuRuiz031/dev-assistant.git
cd dev-assistant
```

Pull the model (Select ONE, depends on how much vram you are able to use. 14b is 9gb vram, 32b is way more. Mistral is a few GB):
```bash
ollama pull qwen2.5:14b
ollama pull qwen2.5:32b
ollama pull mistral
```

Install the Python dependency:
```bash
pip install requests
```

**Set your project path** — open `src/main/resources/prompts/system.st` and update:
```
Project root: /path/to/your/project
Java source root: /path/to/your/project/src/main/java/com/yourpackage
```

---

## Running

You need two terminals.

**Terminal 1 — start the backend:**

Linux:
```bash
./mvnw spring-boot:run
```
Windows:
```
mvnw.cmd spring-boot:run
```

Wait for `Started DevAssistantApplication`.

**Terminal 2 — start the CLI:**

Linux:
```bash
python3 cli.py
```
Windows:
```
python cli.py
```

---

## CLI commands

| Command | Description |
|---------|-------------|
| `new` | Start a fresh conversation |
| `clear` | Clear current conversation history |
| `history` | Show the current conversation ID |
| `quit` | Exit |
| anything else | Send a message to the assistant |

---

## Switching to a different LLM

This repo is wired for the OpenAI-compatible Spring AI starter pointed at Ollama. If you want Anthropic or another provider, you will need to add the matching Spring AI dependency and update the `spring.ai` configuration.

For example, a Claude setup would require changing both the Maven dependency and the YAML config, then setting the provider API key in your shell.

---

## API endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/chat` | Blocking chat |
| `GET` | `/api/chat/stream` | Streaming chat (SSE) |
| `GET` | `/api/chat/{id}/history` | Conversation history |
| `DELETE` | `/api/chat/{id}` | Clear conversation |
| `GET` | `/actuator/health` | Health check |

---

## Known limitations (Alpha)

- Conversation memory is lost on backend restart
- Tool calling reliability varies by model — Mistral works best locally, Claude works best overall
- No authentication — do not expose port 8080 publicly

---

## Built with

- Spring Boot 3.5 + Spring AI 1.0
- Ollama
- Lombok