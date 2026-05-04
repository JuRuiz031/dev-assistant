package com.juanruiz.dev_assistant.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentService {

    private final ChatClient chatClient;
    @NonNull
    private final ChatMemory chatMemory;

    public String chat(String message, String conversationId) {
        Objects.requireNonNull(conversationId, "ConversationId must not be null");

        if (message == null || message.isBlank()) {
            return "Please provide a message, prompts cannot be blank or null";
        }
        log.debug("chat() called: conversationId={}", conversationId);
        return chatClient.prompt()
            .user(message)
            .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(conversationId)
                .build())
            .call()
            .content();
    }

    public List<Map<String, String>> getHistory(String conversationId) {
        Objects.requireNonNull(conversationId, "conversationId must not be null");
    
        List<Map<String, String>> history = chatMemory.get(conversationId)
            .stream()
            .map(msg -> Map.of(
                "role",    msg.getMessageType().getValue(),
                "content", msg.getText()
            ))
            .toList();
    
        log.info("History retrieved for conversationId={}, messages={}", conversationId, history.size());
        return history;
    }
    public boolean clearMemory(String conversationId) {
        Objects.requireNonNull(conversationId, "ConversationId must not be null");

        List<Message> existing = chatMemory.get(conversationId);
        if(existing == null || existing.isEmpty()) {
            log.warn("Attempted to clear non-existent conversation: {}", conversationId);
            return false;
        }

        chatMemory.clear(conversationId);
        log.info("Memory cleared for conversationId: {}", conversationId);
        return true;
    }

    public Flux<String> stream(String message, String conversationId) {
        Objects.requireNonNull(message, "Message must not be null");
        Objects.requireNonNull(conversationId, "ConversationId must not be null");
        return chatClient.prompt()
            .user(message)
            .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(conversationId)
                .build())
            .stream()
            .content()
            .doOnSubscribe(s -> log.info("Stream started: {}", conversationId))
            .doOnComplete(() -> log.info("Stream complete: {}", conversationId))
            .doOnError(e -> log.error("Stream error [{}]: {}", conversationId, e.getMessage()));
    }

}