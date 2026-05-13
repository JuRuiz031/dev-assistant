# CLI Dev Assistant — Alpha

A lightweight agentic developer assistant that runs entirely on your local machine. Powered by Spring AI and Ollama, it connects a local LLM to your filesystem and git history — giving it the ability to read your actual project files, understand your codebase, and answer questions grounded in reality rather than guesswork.

Built to run with Ollama out of the box, but designed to swap to any LLM provider (Claude, OpenAI, Gemini) with minimal configuration changes.

---

## What it can do

- Read and analyze files in any project directory
- List directory structures and navigate your codebase
- Search for files by name or pattern across a project
- Read git history, status, and diffs
- Remember the full context of your conversation
- Stream responses token by token as they're generated
- Maintain multiple independent conversations simultaneously

---

## How it works

```
You (CLI) → Spring Boot REST API → Spring AI → Ollama (local LLM)
                                       ↓
                              FileSystemTools
                              GitTools
                              ChatMemory
```

The CLI sends your messages to the Spring Boot backend. The backend passes them to the LLM along with a set of registered tools. The LLM decides which tools to call, Spring AI executes them, and the results feed back into the response.

---

## Prerequisites

### Everyone
- **Git** — to clone this repo
- **Java 21** — required to run the Spring Boot backend
- **Python 3** — required to run the CLI
- **Ollama** — the local LLM runtime

### Installing Java 21

**Windows:**
1. Go to https://adoptium.net
2. Download the Java 21 installer for Windows
3. Run the installer (next → next → finish)
4. Verify: open Command Prompt and run `java --version` — should show 21.x

**Linux:**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
java --version
```

### Installing Python 3

**Windows:**
1. Go to https://www.python.org/downloads/
2. Download the latest Python 3.x installer
3. Run it — **check "Add Python to PATH"** before clicking Install
4. Verify: open Command Prompt and run `python --version`

**Linux:**
```bash
sudo apt update
sudo apt install python3 python3-pip
python3 --version
```

### Installing Ollama

**Windows:**
1. Go to https://ollama.com/download
2. Download the Windows installer
3. Run it
4. Ollama starts automatically as a background service

**Linux:**
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/JuRuiz031/dev-assistant.git
cd dev-assistant
```

### 2. Pull the LLM model

This project uses `llama3.1` by default. Pull it with:

```bash
ollama pull llama3.1
```

This is a ~4.7GB download. Get a coffee.

### 3. Install Python dependencies

```bash
pip install requests
```

**Windows:**
```
pip install requests
```

### 4. Configure your project path (optional)

Open `src/main/resources/prompts/system.st` and update the project path to point to whatever codebase you want the assistant to work with:

```
Project root: /path/to/your/project
```

---

## Running the app

You need two things running simultaneously: the Spring Boot backend and the CLI.

### Step 1 — Start the backend

**Linux:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```
mvnw.cmd spring-boot:run
```

Wait until you see:
```
Started DevAssistantApplication in X seconds
```

The backend is now running on port 8080.

### Step 2 — Start the CLI (in a new terminal)

**Linux / Mac:**
```bash
python3 cli.py
```

**Windows:**
```
python cli.py
```

---

## Using the CLI

```
Dev Assistant CLI — type 'quit' to exit, 'new' for new conversation

You: what files are in my project?
Assistant: [lists your actual project files]

You: read the pom.xml
Assistant: [reads and explains your actual pom.xml]

You: show me the last 5 commits
Assistant: [shows real git history]

You: new
New conversation started.

You: quit
Bye!
```

### Commands
| Command | Description |
|---------|-------------|
| `new` | Start a fresh conversation (clears context) |
| `quit` | Exit the CLI |
| Any other text | Send a message to the assistant |

---

## Switching to a different LLM

The backend is built with Spring AI's provider abstraction. Swapping models requires two small changes:

### Claude (Anthropic)

**pom.xml** — replace the OpenAI starter:
```xml
<artifactId>spring-ai-starter-model-anthropic</artifactId>
```

**application.yml** — replace the AI config block:
```yaml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      chat:
        options:
          model: claude-sonnet-4-20250514
```

Set your API key as an environment variable:
```bash
export ANTHROPIC_API_KEY=your-key-here   # Linux
set ANTHROPIC_API_KEY=your-key-here      # Windows
```

### OpenAI (GPT-4o)

**pom.xml** — starter is already `spring-ai-starter-model-openai`

**application.yml:**
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o
```

No other code changes needed. Your tools, memory, and streaming all work identically regardless of provider.

---

## Project structure

```
dev-assistant/
├── src/main/java/com/juanruiz/dev_assistant/
│   ├── config/
│   │   ├── AiConfig.java          — ChatClient, memory, tools configuration
│   │   └── WebConfig.java         — CORS configuration
│   ├── controller/
│   │   └── ChatController.java    — REST endpoints
│   ├── service/
│   │   └── AgentService.java      — LLM interaction, streaming, memory
│   ├── tools/
│   │   ├── FileSystemTools.java   — listDirectory, readFile, findFiles
│   │   └── GitTools.java          — gitLog, gitStatus, gitDiff
│   └── model/
│       ├── ChatRequest.java       — Request DTO
│       └── ChatResponse.java      — Response DTO
├── src/main/resources/
│   ├── prompts/system.st          — System prompt (edit this to customize behavior)
│   └── application.yml            — Configuration
├── cli.py                         — Python CLI client
└── pom.xml                        — Maven dependencies
```

---

## API endpoints

If you want to call the backend directly instead of using the CLI:

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/chat` | Send a message, get full response |
| `GET` | `/api/chat/stream` | Send a message, stream response via SSE |
| `GET` | `/api/chat/{id}/history` | Get conversation history |
| `DELETE` | `/api/chat/{id}` | Clear a conversation |
| `GET` | `/actuator/health` | Health check |

---

## Known limitations (Alpha)

- Conversation memory is in-memory only — restarting the backend clears all history
- Tool calling reliability depends on the local model — `llama3.1` works well, smaller models may hallucinate
- No authentication — don't expose port 8080 publicly without adding security
- File access is unrestricted — the assistant can read any file the process has permission to

---

## Built with

- [Spring Boot 3.5](https://spring.io/projects/spring-boot)
- [Spring AI 1.0](https://spring.io/projects/spring-ai)
- [Ollama](https://ollama.com)
- [Lombok](https://projectlombok.org)