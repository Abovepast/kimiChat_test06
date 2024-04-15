package com.example.kimichat_test06.bean;


public class ChatMessage {
    private final String message;
    private final boolean isUser;
    //private long timestamp;
    private long conversationId;

    public ChatMessage(String message, boolean isUser, long conversationId) {
        this.message = message;
        this.isUser = isUser;
        //this.timestamp = System.currentTimeMillis();
        this.conversationId = conversationId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public long getConversationId() {
        return conversationId;
    }
    public void setConversationId(long conversationId) {
        this.conversationId = conversationId;
    }

    public String getTitle() {
        // 返回第一句的内容作为标题
        String[] lines = message.split("\n");
        if (lines.length > 0) {
            return lines[0];
        } else {
            return message;
        }
    }
}
