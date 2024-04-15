package com.example.kimichat_test06;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kimichat_test06.adapter.ChatAdapter;
import com.example.kimichat_test06.bean.ChatMessage;
import com.example.kimichat_test06.bean.Conversation;
import com.example.kimichat_test06.dao.ChatDatabaseHelper;
import com.example.kimichat_test06.service.KimiChatService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.noties.markwon.Markwon;
import io.noties.markwon.image.glide.GlideImagesPlugin;

public class MainActivity extends AppCompatActivity{

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private Button userSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private KimiChatService kimi;
    private boolean isFinish = false;
    private CountDownTimer countDownTimer;
    private String apiKey;
    private long conversationId;
    private Conversation conversation;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        userSend = findViewById(R.id.userSend);
        ImageView otherClear = findViewById(R.id.other_clear);
        ImageView brainClear = findViewById(R.id.brain_clear);
        TextView textBar = findViewById(R.id.TextBar);
        ImageView history_chat = findViewById(R.id.history_chat);
        ImageView eng_learn = findViewById(R.id.eng_learn);

        chatMessages = new ArrayList<>();
        Markwon markwon = Markwon.builder(this)
                .usePlugin(GlideImagesPlugin.create(this))
                .build();

        chatAdapter = new ChatAdapter(chatMessages, markwon, "kunkun");

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        SharedPreferences sharedPreferences = getSharedPreferences("kimiChat", MODE_PRIVATE);
        apiKey = sharedPreferences.getString("API_KEY", "");

        ChatDatabaseHelper chatDatabaseHelper = new ChatDatabaseHelper(this);
        // 初始化AI
        initChat(sharedPreferences, "kunkun");

        // 当用户点击发送按钮时，创建消息并更新 UI
        userSend.setOnClickListener(new View.OnClickListener() {

            private ChatMessage messageBot;
            private String kimiResponse;

            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                // 获取文本并创建消息
                String messageText = messageEditText.getText().toString().trim();
                if (!messageText.isEmpty()) {
                    ChatMessage message = new ChatMessage(messageText, true, conversationId);
                    chatMessages.add(message);

                    messageBot = new ChatMessage("KunKun思考中...", false, conversationId);
                    chatMessages.add(messageBot);

                    textBar.setText("60 S");
                    // 初始化isSkip为false,代表是否完成倒计时或kimiResponse已经接收到了解析结果
                    isFinish = false;
                    // 设置每1秒更新一次textBar的40秒倒计时，当isFinish为true时，停止倒计时
                    startCountdownTimer();

                    // 等待回复途中,禁止发送消息
                    messageEditText.setText("");
                    userSend.setEnabled(false);

                    chatAdapter.notifyItemChanged(chatAdapter.getItemCount()-1);
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount()-1);

                // 开启异步线程，发送消息
                new Thread(() -> {
                    try {
                        kimiResponse = kimi.sendRequestWithOkHttp(messageText, apiKey);
                        isFinish = true;
                        // 在这里，你可以将kimiResponse保存在一个全局变量或者通过其他方式传递回主线程
                    } catch (IOException e) {
                        Log.e("MainActivity", "Error: " + e.getMessage());
                        kimiResponse = "Error!\n错误信息:" + e;
                    }

                    // 在主线程中更新UI
                    runOnUiThread(() -> {

                        chatMessages.remove(messageBot);
                        messageBot = new ChatMessage(kimiResponse, false, conversationId);
                        chatMessages.add(messageBot);
                        //tempMessageList.add(messageBot);

                        textBar.setText(R.string.jinjincainiao_ai);
                        userSend.setEnabled(true);

                        // chatAdapter.notifyDataSetChanged();
                        chatAdapter.notifyItemChanged(chatAdapter.getItemCount()-1);
                        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount()-1);
                        // 保存当前对话到历史记录
                        //AddAMsgToSQLite(messageBot, chatDatabaseHelper);
                    });
                }).start();

                // 保存当前对话到历史记录
                } else {
                    Toast.makeText(MainActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                }

            }
            private void startCountdownTimer() {

                // 如果上一个计时器在活动，取消计时器
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                countDownTimer = new CountDownTimer(60000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (!isFinish) {
                            // Update textBar every second
                            textBar.setText(String.format(Locale.getDefault(), "%d S", millisUntilFinished / 1000));
                        }
                    }
                    @Override
                    public void onFinish() {
                        if (!isFinish) {
                            // 未收到响应时处理超时
                            messageBot = new ChatMessage("KunKun的CPU被干烧了，请重新提问吧", false, conversationId);
                            userSend.setEnabled(true);
                            // 更新并保存消息
                            //AddAMsgToSQLite(messageBot, chatDatabaseHelper);
                        }
                    }
                };

                countDownTimer.start();
            }
        });
        // 设置页面
        otherClear.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
        //清除页面
        otherClear.setOnLongClickListener(v -> {
            dialogClear();
            Toast.makeText(MainActivity.this, "对话内容清除成功", Toast.LENGTH_SHORT).show();
            return true;
        });
        // 清空对话
        brainClear.setOnClickListener(v -> {
            // 保存之前的聊天记录
            saveConversation(chatDatabaseHelper);
            /*if(!conversation.getChatMessages().isEmpty()) {
                chatDatabaseHelper.saveConversation(conversation);
                dialogClear();
                Toast.makeText(this, "对话已保存", Toast.LENGTH_SHORT).show();
            }*/
            //dialogClear();
            // 开启新对话
            initChat(sharedPreferences, "kunkun");
            //dialogClear();
            String tipStr = "哎哟，你干嘛~~~，KunKun已重新启动！让我们来开始新的对话吧！";
            ChatMessage tipInfo = new ChatMessage(tipStr, false, conversationId);

            chatMessages.add(tipInfo);
            chatAdapter.notifyItemChanged(chatAdapter.getItemCount()-1);
            chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount()-1);
            //AddAMsgToSQLite(tipInfo, chatDatabaseHelper);

            //改变UI
            userSend.setBackground(getDrawable(R.drawable.send_button_select));
        });
        // 历史对话
        history_chat.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
        // 英语学习模式
        eng_learn.setOnClickListener(v->{
            saveConversation(chatDatabaseHelper);
            //dialogClear();
            initChat(sharedPreferences, "english");
            String tipStr = "KunKun已进入英文句子分析模式!\n" +
                    "直接输入英文句子,我将会分析句子成分。";
            ChatMessage tipInfo = new ChatMessage(tipStr, false, conversationId);

            chatMessages.add(tipInfo);
            chatAdapter.notifyItemChanged(chatAdapter.getItemCount()-1);
            chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount()-1);

            // 保存对话内容
            //AddAMsgToSQLite(tipInfo, chatDatabaseHelper);

            //改变UI
            userSend.setBackground(getDrawable(R.drawable.user_send_english));
        });
    }

    private void saveConversation(ChatDatabaseHelper chatDatabaseHelper) {
        if(!conversation.getChatMessages().isEmpty()){
            if(conversation.getChatMessages().size()>1) {
                chatDatabaseHelper.saveConversation(conversation);
                Toast.makeText(this, "对话已保存", Toast.LENGTH_SHORT).show();
            }
            dialogClear();
        }
    }

    private void initChat(SharedPreferences sharedPreferences, String ChatMode) {
        apiKey = sharedPreferences.getString("API_KEY", "");
        chatAdapter.ChatMode = ChatMode;
        long tempStartTimeStamp = System.currentTimeMillis();
        long tempConversationId = RandomCID(tempStartTimeStamp);
        conversation = new Conversation(tempConversationId, tempStartTimeStamp, chatMessages, "KunKun");
        conversationId = conversation.getConversationId();  // 设置新的对话ID
        kimi = new KimiChatService(ChatMode);
    }

    private static long RandomCID(long tempStartTimeStamp) {
        long randomNumber = (long) (Math.random() * 1000000L);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        Date date = new Date(tempStartTimeStamp);// 将long时间戳转换为Date对象
        String formattedDateString = formatter.format(date);// 将Date对象格式化为字符串
        return Long.parseLong(formattedDateString)*1000000L+randomNumber;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void dialogClear() {
        chatMessages.clear(); // 清除所有消息
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
    }

}