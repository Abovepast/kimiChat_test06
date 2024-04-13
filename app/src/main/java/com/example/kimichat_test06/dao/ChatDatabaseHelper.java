package com.example.kimichat_test06.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.kimichat_test06.bean.ChatMessage;

public class ChatDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "KunKunChat.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CHAT_MESSAGES = "chat_messages";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_MESSAGE_TEXT = "message_text";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_IS_USER = "is_user";

    private static final String CREATE_TABLE_CHAT_MESSAGES =
            "CREATE TABLE " + TABLE_CHAT_MESSAGES + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_MESSAGE_TEXT + " TEXT NOT NULL," +
                    COLUMN_TIMESTAMP + " INTEGER NOT NULL," +
                    COLUMN_IS_USER + " INTEGER NOT NULL" +
                    ")";

    public ChatDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CHAT_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 在这里添加数据迁移逻辑，例如当数据库版本升级时，更新表结构或数据
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_MESSAGES);
        onCreate(db);
    }

    // 添加插入聊天消息的方法
    public void insertChatMessage(ChatMessage chatMessage) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE_TEXT, chatMessage.getMessage());
        chatMessage.setTimestamp(); // 设置消息时间戳
        values.put(COLUMN_TIMESTAMP, chatMessage.getTimestamp());
        values.put(COLUMN_IS_USER, chatMessage.isUser() ? 1 : 0);

        db.insert(TABLE_CHAT_MESSAGES, null, values);
        db.close();
    }

    // 添加查询、更新、删除等其他方法...
    // 获取所有聊天消息
    public Cursor getAllChatMessages() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CHAT_MESSAGES;
        return db.rawQuery(query, null);
    }

    // 添加更新聊天消息的方法
    public int updateChatMessage(ChatMessage chatMessage) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE_TEXT, chatMessage.getMessage());
        values.put(COLUMN_TIMESTAMP, chatMessage.getTimestamp());
        values.put(COLUMN_IS_USER, chatMessage.isUser() ? 1 : 0);

        int rowsAffected = db.update(TABLE_CHAT_MESSAGES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(chatMessage.getConversationId())});
        db.close();
        return rowsAffected;
    }

    // 添加删除聊天消息的方法
    public int deleteChatMessage(ChatMessage chatMessage) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_CHAT_MESSAGES, COLUMN_ID + " = ?", new String[]{String.valueOf(chatMessage.getConversationId())});
        db.close();
        return rowsDeleted;
    }
}