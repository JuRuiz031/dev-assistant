package com.juanruiz.dev_assistant.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    @NotBlank(message = "Message is required")
    private String message;

    @NotBlank(message = "Conversation ID is required")
    private String conversationId;
}
