package com.example.kimichat_test06;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kimichat_test06.adapter.ChatAdapter;
import com.example.kimichat_test06.bean.ChatMessage;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.image.glide.GlideImagesPlugin;

public class HistoryActivity extends AppCompatActivity {
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        ListView list = findViewById(R.id.historyText);
        chatMessages = new ArrayList<>();
        Markwon markwon = Markwon.builder(this)
                .usePlugin(GlideImagesPlugin.create(this))
                .build();
        chatAdapter = new ChatAdapter(chatMessages, markwon, false);

        ImageView back_to_main = findViewById(R.id.back_to_main);

        //---------------逻辑实现-----------------------------//
        // 返回按钮
        back_to_main.setOnClickListener(v -> finish());
    }
}