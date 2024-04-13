package com.example.kimichat_test06.bean;

public class Conversation {
    private long conversationId;
    private long startTimeStamp;

//    public Conversation(long conversationId, long startTimeStamp)
//    {
//        this.conversationId = conversationId;
//        this.startTimeStamp = startTimeStamp;
//    }

    public long getConversationId() {
        return conversationId;
    }

    public void setConversationId() {
        // 设置一个随机数
        this.conversationId = (long) (Math.random() * 1000000000000000L);
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp() {
        // 设置为当前时间
        this.startTimeStamp = System.currentTimeMillis();
    }
}
