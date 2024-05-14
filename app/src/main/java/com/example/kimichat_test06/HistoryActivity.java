package com.example.kimichat_test06;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kimichat_test06.adapter.ChatAdapter;
import com.example.kimichat_test06.adapter.ConversationAdapter;
import com.example.kimichat_test06.adapter.OnItemClickListener;
import com.example.kimichat_test06.bean.ChatMessage;
import com.example.kimichat_test06.bean.Conversation;
import com.example.kimichat_test06.dao.ChatDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.image.glide.GlideImagesPlugin;

public class HistoryActivity extends AppCompatActivity implements OnItemClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        ListView list = findViewById(R.id.historyText);
        List<ChatMessage> chatMessages = new ArrayList<>();
        Markwon markwon = Markwon.builder(this)
                .usePlugin(GlideImagesPlugin.create(this))
                .build();
        new ChatAdapter(chatMessages, markwon, "kunkun", this);

        ImageView back_to_main = findViewById(R.id.back_to_main);
        TextView no_history = findViewById(R.id.no_history);

        try (ChatDatabaseHelper chatDatabaseHelper = new ChatDatabaseHelper(this)){
            List<Conversation> conversations = chatDatabaseHelper.getAllConversations();
            if(!conversations.isEmpty()) {
                no_history.setVisibility(View.GONE);
                ConversationAdapter adapter = new ConversationAdapter(this, conversations);
                list.setAdapter(adapter);
            } else {
                no_history.setVisibility(View.VISIBLE);
            }
        }

        //---------------逻辑实现-----------------------------//
        // 返回按钮
        back_to_main.setOnClickListener(v -> finish());
    }

    @Override
    public void onItemClick(String data) {

    }
}