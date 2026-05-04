package com.juanruiz.dev_assistant.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.juanruiz.dev_assistant.model.ChatRequest;
import com.juanruiz.dev_assistant.model.ChatResponse;
import com.juanruiz.dev_assistant.service.AgentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {
    private final AgentService agentService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest chatRequest) {
        long start = System.currentTimeMillis();
        log.info("POST /api/chat  conversationId={}", chatRequest.getConversationId());

        String reply = agentService.chat(chatRequest.getMessage(), chatRequest.getConversationId());

        return ResponseEntity.ok(new ChatResponse(reply, chatRequest.getConversationId(), System.currentTimeMillis() - start));
    }

    @GetMapping("/{conversationId}/history")
    public ResponseEntity<List<Map<String, String>>> history(@PathVariable String conversationId) {
        log.info("GET /api/chat/{}/history", conversationId);
        return ResponseEntity.ok(agentService.getHistory(conversationId));
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> clearHistory(@PathVariable String conversationId) {
        log.info("DELETE /api/chat/{}/history", conversationId);
        boolean cleared = agentService.clearMemory(conversationId);

        if(cleared) {
            log.info("Conversation cleared successfully: {}", conversationId);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Conversation not found: {}", conversationId);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String message, @RequestParam String conversationId) {
        log.info("GET /api/chat/stream  conversationId={}", conversationId);
        return agentService.stream(message, conversationId);
    }
}
