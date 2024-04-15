package com.example.kimichat_test06;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kimichat_test06.adapter.ChatAdapter;
import com.example.kimichat_test06.bean.ChatMessage;
import com.example.kimichat_test06.dao.ChatDatabaseHelper;

import java.util.List;

import io.noties.markwon.Markwon;

public class ShowConversation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_conversation);

        ImageView back = findViewById(R.id.back);
        RecyclerView showRecyclerView = findViewById(R.id.showRecyclerView);
        TextView testText = findViewById(R.id.testText);

        testText.setText(getIntent().getStringExtra("startTimeStamp"));

        try(ChatDatabaseHelper chatDatabaseHelper = new ChatDatabaseHelper(this)) {
            List<ChatMessage> chatMessages = chatDatabaseHelper.getChatMessages_cid(getIntent().getLongExtra("conversationId", 0));
            ChatAdapter chatAdapter = new ChatAdapter(chatMessages, Markwon.create(this), getIntent().getStringExtra("conversationName"));
            showRecyclerView.setAdapter(chatAdapter);
        }

        // 返回按钮
        back.setOnClickListener(v -> finish());
    }
}