package com.example.kimichat_test06.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.kimichat_test06.bean.ChatMessage;
import com.example.kimichat_test06.bean.Conversation;

import java.util.ArrayList;
import java.util.List;

public class ChatDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "KunKunChat.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CHAT_MESSAGES = "chat_messages";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_MESSAGE_TEXT = "message_text";

    private static final String COLUMN_IS_USER = "is_user";

    private static final String COLUMN_CONVERSATION_ID = "conversation_id";

    private static final String CREATE_TABLE_CHAT_MESSAGES =
            "CREATE TABLE " + TABLE_CHAT_MESSAGES + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_MESSAGE_TEXT + " TEXT NOT NULL," +
                    COLUMN_IS_USER + " INTEGER NOT NULL," +
                    COLUMN_CONVERSATION_ID + " INTEGER NOT NULL"+
                    ")";

    // 创建一张表用于保存所有的conversationID
    private static final String TABLE_CONVERSATION = "conversation";
    private static final String COLUMN_CONVERSATION = "_id";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_CONPAYERNAME = "conPayerName";
    //private static final String COLUMN_CONVERSATION_ID_VALUE = "conversation_id_value";
    private static final String CREATE_TABLE_CONVERSATION =
            "CREATE TABLE " + TABLE_CONVERSATION + "(" +
                    COLUMN_CONVERSATION + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    // COLUMN_CONVERSATION_ID 是外键
                    COLUMN_CONVERSATION_ID + " INTEGER NOT NULL,"+
                    COLUMN_TIMESTAMP + " INTEGER NOT NULL," +
                    COLUMN_CONPAYERNAME + " TEXT NOT NULL" +
                    ")";

    public ChatDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CHAT_MESSAGES);
        db.execSQL(CREATE_TABLE_CONVERSATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 在这里添加数据迁移逻辑，例如当数据库版本升级时，更新表结构或数据
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_MESSAGES);
        onCreate(db);
    }

    // 添加查询、更新、删除等其他方法...
    // 获取所有聊天消息
//    public Cursor getAllChatMessages() {
//        SQLiteDatabase db = getReadableDatabase();
//        String query = "SELECT * FROM " + TABLE_CHAT_MESSAGES;
//        return db.rawQuery(query, null);
//    }

    // 将谈话记录Conversation对象保存到数据库
    public void saveConversation(Conversation conversation) {
        SQLiteDatabase db = getWritableDatabase();
        for (ChatMessage chatMessage : conversation.getChatMessages()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_MESSAGE_TEXT, chatMessage.getMessage());
//            chatMessage.setTimestamp(); // 设置消息时间戳
//            values.put(COLUMN_TIMESTAMP, chatMessage.getTimestamp());
            values.put(COLUMN_IS_USER, chatMessage.isUser() ? 1 : 0);
            values.put(COLUMN_CONVERSATION_ID, conversation.getConversationId());
            db.insert(TABLE_CHAT_MESSAGES, null, values);
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONVERSATION_ID, conversation.getConversationId());
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
        values.put(COLUMN_CONPAYERNAME, conversation.getConPayerName());
        db.insert(TABLE_CONVERSATION, null, values);
        db.close();
    }

    // 只保存最后一条聊天记录
    public void saveLastChatMessage(ChatMessage chatMessage, long conversationId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE_TEXT, chatMessage.getMessage());
        values.put(COLUMN_IS_USER, chatMessage.isUser() ? 1 : 0);
        values.put(COLUMN_CONVERSATION_ID, conversationId);
        db.insert(TABLE_CHAT_MESSAGES, null, values);
        db.close();
    }
    @SuppressLint("Range")
    public List<ChatMessage> getChatMessages_cid(long conversationId) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CHAT_MESSAGES + " WHERE " + COLUMN_CONVERSATION_ID + " = " + conversationId;
        Cursor cursor = db.rawQuery(query, null);
        List<ChatMessage> chatMessages = new ArrayList<>();
        while (cursor.moveToNext()) {
            String messageText = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_TEXT));
            boolean isUser = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_USER)) == 1;
            ChatMessage chatMessage = new ChatMessage(messageText, isUser, conversationId);
            chatMessages.add(chatMessage);
        }
        cursor.close();
        return chatMessages;
    }

    @SuppressLint("Range")
    public List<Conversation> getAllConversations() {
        // 查找所有的conversationID并在聊天记录表中查找对应的聊天记录
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CONVERSATION;
        Cursor cursor = db.rawQuery(query, null);
        // 使用一个Conversation来存储ID相同的聊天记录
        List<Conversation> conversationList = new ArrayList<>();
        while (cursor.moveToNext()) {
            long conversationId = cursor.getLong(cursor.getColumnIndex(COLUMN_CONVERSATION_ID));
            long timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP));
            List<ChatMessage> chatMessages = getChatMessages_cid(conversationId);
            String conPayerName = cursor.getString(cursor.getColumnIndex(COLUMN_CONPAYERNAME));
            conversationList.add(new Conversation(conversationId, timestamp, chatMessages, conPayerName));
        }
        cursor.close();
        return conversationList;
    }

    public void deleteConversation(long conversationId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CONVERSATION, COLUMN_CONVERSATION_ID + " = ?", new String[]{String.valueOf(conversationId)});
    }

    // 保存并更新的对话
}