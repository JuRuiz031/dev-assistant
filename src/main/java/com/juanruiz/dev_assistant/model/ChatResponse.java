package com.juanruiz.dev_assistant.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResponse {
    private String reply;
    private String conversationId;
    private long processingTimeMs;
}
