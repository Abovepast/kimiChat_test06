package com.example.kimichat_test06.bean;

import java.util.List;

public class Conversation {
    private long conversationId;
    private long startTimeStamp;
    private List<ChatMessage> chatMessages;
    private String conPayerName;

    public Conversation(long conversationId, long startTimeStamp, List<ChatMessage> chatMessages, String conPayerName)
    {
        //this.conversationId = conversationId;
        this.conversationId = conversationId;
        this.startTimeStamp = startTimeStamp;
        this.chatMessages = chatMessages;
        this.conPayerName = conPayerName;
    }

    public long getConversationId() {
        return conversationId;
    }

    public void setConversationId(long conversationId) {
        // 设置一个随机数
        this.conversationId = conversationId;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(long startTimeStamp) {
        // 设置为当前时间
        this.startTimeStamp = startTimeStamp;
    }

    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public String getConPayerName() {
        return conPayerName;
    }

    public void setConPayerName(String conPayerName) {
        this.conPayerName = conPayerName;
    }

    public ChatMessage getLastMsg()
    {
        return chatMessages.get(chatMessages.size() - 1);
    }
}
