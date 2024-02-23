package com.example.kimichat_test06;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
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
    private boolean isFinsh = false;
    private CountDownTimer countDownTimer;

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

        chatMessages = new ArrayList<>();
        Markwon markwon = Markwon.builder(this)
                .usePlugin(GlideImagesPlugin.create(this))
                .build();
        chatAdapter = new ChatAdapter(chatMessages, markwon);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // 初始化AI
        kimi = new KimiChatService();

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
                    ChatMessage message = new ChatMessage(messageText, true);
                    chatMessages.add(message);

                    messageBot = new ChatMessage("KunKun思考中...", false);
                    chatMessages.add(messageBot);

                    textBar.setText("60 S");
                    // 初始化isSkip为false,代表是否完成倒计时或kimiResonpse已经接收到了解析结果
                    isFinsh = false;
                    // 设置每1秒更新一次textBar的40秒倒计时，当isFinsh为true时，停止倒计时
                    startCountdownTimer();

                    messageEditText.setText("");
                    userSend.setEnabled(false);

                    chatAdapter.notifyItemChanged(chatAdapter.getItemCount()-1);
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount()-1);

                // 开启异步线程，发送消息
                // 在新线程中
                new Thread(() -> {
                    try {
                        kimiResponse = kimi.sendRequestWithOkHttp(messageText);
                        isFinsh = true;
                        // 在这里，你可以将kimiResponse保存在一个全局变量或者通过其他方式传递回主线程
                    } catch (IOException e) {
                        e.printStackTrace();
                        kimiResponse = "Error!\n错误信息:" + e;
                    }

                    // 在主线程中更新UI
                    runOnUiThread(() -> {

                        chatMessages.remove(messageBot);
                        messageBot = new ChatMessage(kimiResponse, false);
                        chatMessages.add(messageBot);

                        textBar.setText(R.string.jinjincainiao_ai);
                        userSend.setEnabled(true);
//                        chatAdapter.notifyDataSetChanged();
                        chatAdapter.notifyItemChanged(chatAdapter.getItemCount()-1);
                        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount()-1);
                    });
                }).start();
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
                        if (!isFinsh) {
                            // Update textBar every second
                            textBar.setText(String.format(Locale.getDefault(), "%d S", millisUntilFinished / 1000));
                        }
                    }
                    @Override
                    public void onFinish() {
                        if (!isFinsh) {
                            // Handle timeout when no response is received
                            messageBot = new ChatMessage("KunKun的CPU被干烧了，请重新提问吧", false);
                            chatMessages.add(messageBot);
                            userSend.setEnabled(true);
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
            kimi = new KimiChatService();
            dialogClear();
            Toast.makeText(MainActivity.this, "kunkun大脑已清空！", Toast.LENGTH_SHORT).show();
            String tipStr = "哎哟，你干嘛~~~，KunKun已重新启动！让我们来开始新的对话吧！";
            ChatMessage tipInfo = new ChatMessage(tipStr, false);
            chatMessages.add(tipInfo);
            chatAdapter.notifyItemChanged(chatAdapter.getItemCount()-1);
            chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount()-1);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void dialogClear() {
        chatMessages.clear(); // 清除所有消息
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount());
    }

}