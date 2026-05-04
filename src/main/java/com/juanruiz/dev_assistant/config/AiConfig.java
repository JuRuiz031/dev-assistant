package com.juanruiz.dev_assistant.config;

import java.util.Objects;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.juanruiz.dev_assistant.tools.FileSystemTools;
import com.juanruiz.dev_assistant.tools.GitTools;

@Configuration
public class AiConfig {
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
            .maxMessages(40)
            .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory memory, FileSystemTools fileSystemTools, GitTools gitTools) {
        Objects.requireNonNull(memory, "ChatMemory must not be null");
        Objects.requireNonNull(fileSystemTools, "FileSystemTools must not be null");
        Objects.requireNonNull(gitTools, "GitTools must not be null");

        Resource systemPrompt = new ClassPathResource("prompts/system.st");

        return builder
            .defaultSystem(systemPrompt)
            .defaultTools(fileSystemTools, gitTools)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
            .build();
    }
}
