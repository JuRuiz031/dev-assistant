# Dev Assistant (Alpha)

A lightweight agentic developer assistant combining a Spring Boot REST API backend with a Python CLI client. Runs entirely locally, powered by Spring AI and Ollama. Reads your actual project files, navigates your codebase, and grounds responses in reality rather than training data.

Built for Ollama out of the box.

---

## Capabilities

- Navigate and list project structure
- Read source code, configs, and documentation
- Search for files by pattern
- Access git history, status, and diffs
- Maintain multi-turn conversation context with memory
- Stream responses token by token

---

## Prerequisites

- Java 21
- Python 3
- Ollama (https://ollama.ai)

**Install Java 21:**

Linux:
```bash
sudo apt install openjdk-21-jdk
```

Windows: Download from https://adoptium.net

---

## Setup

Clone and configure:
```bash
git clone https://github.com/JuRuiz031/dev-assistant.git
cd dev-assistant
```

Pull a model (choose one based on available VRAM):
```bash
ollama pull qwen2.5:14b    # ~9GB
ollama pull mistral        # ~5GB
```

Install Python dependencies:
```bash
pip install requests
```

Update project paths in `src/main/resources/prompts/system.st`:
```
Project root: /path/to/your/project
Java source root: /path/to/your/project/src/main/java/com/yourpackage
```

---

## Running

**Terminal 1 — Backend (REST API on port 8080):**

Linux:
```bash
./mvnw spring-boot:run
```

Windows:
```
mvnw.cmd spring-boot:run
```

**Terminal 2 — CLI Client:**

Linux:
```bash
python3 cli.py
```

Windows:
```
python cli.py
```

---

## CLI Commands

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
- Smaller models may hallucinate or respond in a different language (language choice depends on model size)
- No authentication — do not expose port 8080 publicly

---

## Built with

- Spring Boot 3.5 + Spring AI 1.0
- Ollama
- Lombok