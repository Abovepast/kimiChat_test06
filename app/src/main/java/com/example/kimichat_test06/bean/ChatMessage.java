package com.example.kimichat_test06.bean;


public class ChatMessage {
    private final String message;
    private final boolean isUser;

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

}
