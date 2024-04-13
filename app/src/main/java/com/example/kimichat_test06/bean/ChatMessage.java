package com.example.kimichat_test06.bean;


public class ChatMessage {
    private final String message;
    private final boolean isUser;
    private long timestamp;
    private long conversationId;

    public ChatMessage(String message, boolean isUser, long conversationId) {
        this.message = message;
        this.isUser = isUser;
        this.setTimestamp();
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }
    public long getConversationId() {
        return conversationId;
    }
    public void setConversationId(long conversationId) {
        this.conversationId = conversationId;
    }

}
