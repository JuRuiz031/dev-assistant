#!/usr/bin/env python3
"""
CLI Dev Assistant — Alpha
A local agentic developer assistant powered by Spring AI and Ollama.
"""

import requests
import uuid
import sys

BASE_URL = "http://localhost:8080"
CONV_ID = str(uuid.uuid4())


def stream_message(message: str, conversation_id: str) -> None:
    """Send a message and stream the response token by token."""
    try:
        with requests.get(
            f"{BASE_URL}/api/chat/stream",
            params={"message": message, "conversationId": conversation_id},
            stream=True,
            timeout=120
        ) as response:
            if response.status_code != 200:
                print(f"Error: server returned {response.status_code}")
                return

            print("Assistant: ", end="", flush=True)
            for line in response.iter_lines():
                if line:
                    decoded = line.decode("utf-8")
                    if decoded.startswith("data:"):
                        token = decoded[5:].strip()
                        if token:
                            print(token, end="", flush=True)
            print("\n")

    except requests.exceptions.ConnectionError:
        print("\nError: could not connect to the backend.")
        print("Make sure the Spring Boot app is running on port 8080.")
        print("  Linux:   ./mvnw spring-boot:run")
        print("  Windows: mvnw.cmd spring-boot:run\n")
    except requests.exceptions.Timeout:
        print("\nError: request timed out. The model may be overloaded.\n")
    except KeyboardInterrupt:
        print("\n")


def clear_conversation(conversation_id: str) -> None:
    """Clear the current conversation from backend memory."""
    try:
        response = requests.delete(f"{BASE_URL}/api/chat/{conversation_id}", timeout=10)
        if response.status_code == 204:
            print("Conversation cleared.\n")
        elif response.status_code == 404:
            print("No conversation to clear.\n")
    except requests.exceptions.ConnectionError:
        pass


def check_backend() -> bool:
    """Check if the backend is running before starting."""
    try:
        response = requests.get(f"{BASE_URL}/actuator/health", timeout=5)
        return response.status_code == 200
    except requests.exceptions.ConnectionError:
        return False


def print_help() -> None:
    print("""
Commands:
  new      — start a fresh conversation
  clear    — clear current conversation history
  history  — show current conversation ID
  help     — show this message
  quit     — exit
""")


def main():
    global CONV_ID

    print("=" * 50)
    print("  CLI Dev Assistant — Alpha")
    print("=" * 50)

    # Check backend is up
    if not check_backend():
        print("\nError: backend is not running.")
        print("Start it first:")
        print("  Linux:   ./mvnw spring-boot:run")
        print("  Windows: mvnw.cmd spring-boot:run")
        sys.exit(1)

    print(f"\nBackend connected. Conversation ID: {CONV_ID}")
    print("Type 'help' for commands, 'quit' to exit.\n")

    while True:
        try:
            user_input = input("You: ").strip()
        except (KeyboardInterrupt, EOFError):
            print("\nBye!")
            break

        if not user_input:
            continue

        command = user_input.lower()

        if command == "quit":
            print("Bye!")
            break

        elif command == "new":
            clear_conversation(CONV_ID)
            CONV_ID = str(uuid.uuid4())
            print(f"New conversation started. ID: {CONV_ID}\n")

        elif command == "clear":
            clear_conversation(CONV_ID)

        elif command == "history":
            print(f"Current conversation ID: {CONV_ID}\n")

        elif command == "help":
            print_help()

        else:
            stream_message(user_input, CONV_ID)


if __name__ == "__main__":
    main()