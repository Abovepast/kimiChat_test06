package com.example.kimichat_test06;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kimichat_test06.adapter.ChatAdapter;
import com.example.kimichat_test06.adapter.OnItemClickListener;
import com.example.kimichat_test06.bean.ChatMessage;
import com.example.kimichat_test06.bean.Conversation;
import com.example.kimichat_test06.dao.ChatDatabaseHelper;
import com.example.kimichat_test06.service.KimiChatService;
import com.example.kimichat_test06.setting.TtsSettings;
import com.example.kimichat_test06.utils.SpeechApp;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.noties.markwon.Markwon;
import io.noties.markwon.image.glide.GlideImagesPlugin;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnItemClickListener {

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
    //-----------语音合成导入变量-----------//
    private static final String TAG = MainActivity.class.getSimpleName();
    // 语音合成对象
    private SpeechSynthesizer mTts;

    // 默认发音人
    private String voicer = "xiaoqi";

    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue;
    private String texts = "";

    // 缓冲进度
    // private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;

    /*// 云端/本地单选按钮
    private RadioGroup mRadioGroup;
    // 引擎类型
    private final String mEngineType = SpeechConstant.TYPE_CLOUD;*/

    private Toast mToast;
    private SharedPreferences mSharedPreferences;
    // private File pcmFile;
    //------------------------------------//

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

        //-----------语音合成变量--------------//
        TextView tts_text = findViewById(R.id.tts_text);

        chatMessages = new ArrayList<>();
        Markwon markwon = Markwon.builder(this)
                .usePlugin(GlideImagesPlugin.create(this))
                .build();

        chatAdapter = new ChatAdapter(chatMessages, markwon, "kunkun", this);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        SharedPreferences sharedPreferences = getSharedPreferences("kimiChat", MODE_PRIVATE);
        apiKey = sharedPreferences.getString("API_KEY", "");

        // ChatDatabaseHelper chatDatabaseHelper = new ChatDatabaseHelper(this);
        try (ChatDatabaseHelper chatDatabaseHelper = new ChatDatabaseHelper(this)) {
            // 清空对话
            brainClear.setOnClickListener(v -> {
                // 保存之前的聊天记录
                saveConversation(chatDatabaseHelper);
                //dialogClear();
                // 开启新对话
                initChat(sharedPreferences, "kunkun");
                //dialogClear();
                String tipStr = "KunKun已重新启动！你可以向我提出任何问题。";
                ChatMessage tipInfo = new ChatMessage(tipStr, false, conversationId);

                chatMessages.add(tipInfo);
                chatAdapter.notifyItemChanged(chatAdapter.getItemCount()-1);
                chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount()-1);

                //改变UI
                userSend.setBackground(getDrawable(R.drawable.send_button_select));
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

                //改变UI
                userSend.setBackground(getDrawable(R.drawable.user_send_english));
            });
        }

        // 初始化AI
        initChat(sharedPreferences, "kunkun");
        //----------语音合成初始化5.13-----------//
        initVoiceMode();
        //-------------------------------------//

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
                        tts_text.setText(chatAdapter.getItem());

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

        // 历史对话
        history_chat.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

    }

    private void initVoiceMode() {
//        findViewById(R.id.userPlay).setOnClickListener(this);   // 播放
//        findViewById(R.id.userStop).setOnClickListener(this); // 取消
        findViewById(R.id.userSetting).setOnClickListener(this); // 设置
        findViewById(R.id.userPronunciation).setOnClickListener(this); // 发音人选择

        // 初始化
        SpeechApp.initializeMsc(this);
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
        // 云端发音人名称列表
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);
        mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, MODE_PRIVATE);
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
        conversation = new Conversation(tempConversationId, tempStartTimeStamp, chatMessages, ChatMode);
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

    @Override
    public void onClick(View view) {
        if (null == mTts) {
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            this.showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }

        int id = view.getId();
        if (id == R.id.userSetting) {
            Intent intent = new Intent(MainActivity.this, TtsSettings.class);
            startActivity(intent);
            // 开始合成
            // 收到onCompleted 回调时，合成结束、生成合成音频
            // 合成的音频格式：只支持pcm格式
        }
        /*else if (id == R.id.userPlay) {
            // pcmFile = new File(Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath(), "tts_pcmFile.pcm");
            // pcmFile.delete();
            texts = ((TextView) findViewById(R.id.tts_text)).getText().toString();
            // 设置参数
            setParam();
            // 合成并播放
            int code = mTts.startSpeaking(texts, mTtsListener);

            if (code != ErrorCode.SUCCESS) {
                showTip("语音合成失败,错误码: " + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
            // 取消合成
        } else if (id == R.id.userStop) {
            mTts.stopSpeaking();
            // 暂停播放
        }*/
        /*else if (id == R.id.tts_pause) {
            mTts.pauseSpeaking();
            // 继续播放
        } else if (id == R.id.tts_resume) {
            mTts.resumeSpeaking();
            // 选择发音人
        } */
        else if (id == R.id.userPronunciation) {
            showPersonSelectDialog();
        }

    }

    private int selectedNum = 0;

    /**
     * 发音人选择。
     */
    private void showPersonSelectDialog() {
        // kunkun默认在线合成
        new AlertDialog.Builder(this).setTitle("在线合成发音人选项")
                .setSingleChoiceItems(mCloudVoicersEntries, // 单选框有几项,各是什么名字
                        selectedNum, // 默认的选项
                        new DialogInterface.OnClickListener() { // 点击单选框后的处理
                            public void onClick(DialogInterface dialog,
                                                int which) { // 点击了哪一项
                                voicer = mCloudVoicersValue[which];
//                                if ("catherine".equals(voicer) || "henry".equals(voicer) || "vimary".equals(voicer)) {
//                                    ((TextView) findViewById(R.id.tts_text)).setText(R.string.text_tts_source_en);
//                                } else {
//                                    ((TextView) findViewById(R.id.tts_text)).setText(R.string.text_tts_source);
//                                }
                                selectedNum = which;
                                dialog.dismiss();
                            }
                        }).show();
        // 选择在线合成
        /*if (mRadioGroup.getCheckedRadioButtonId() == R.id.tts_radioCloud) {

        }*/
    }

    /**
     * 初始化监听。
     */
    private final InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            } else {
                Log.d(TAG, "初始化成功");
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    /**
     * 合成回调监听。
     */
    private final SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            Log.e("MscSpeechLog_", "percent =" + percent);
            // mPercentForBuffering = percent;
            /*showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));*/
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            Log.e("MscSpeechLog_", "percent =" + percent);
            mPercentForPlaying = percent;
            /*showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));*/

            SpannableStringBuilder style = new SpannableStringBuilder(texts);
            Log.e(TAG, "beginPos = " + beginPos + "  endPos = " + endPos);
            style.setSpan(new BackgroundColorSpan(Color.RED), beginPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((TextView) findViewById(R.id.tts_text)).setText(style);
        }

        @Override
        public void onCompleted(SpeechError error) {
            showTip("播放完成");
            showTip(String.valueOf(mPercentForPlaying));
            if (error != null) {

                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            /**
             * 无关紧要的代码, 简化掉~~
             * 2024年5月14日21:53:01
             */
            //	 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	 若使用本地能力，会话id为null
            /*if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Log.d(TAG, "session id =" + sid);
            }
            // 当设置 SpeechConstant.TTS_DATA_NOTIFY 为1时，抛出buf数据
            if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
                byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
                if (buf != null) {
                    Log.e(TAG, "EVENT_TTS_BUFFER = " + buf.length);
                }
                // 保存文件
                // appendFile(pcmFile, buf);
            }*/

        }
    };

    private void showTip(final String str) {
        runOnUiThread(() -> {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
            mToast.show();
        });
    }

    /**
     * 参数设置
     * return
     */
    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 支持实时音频返回，仅在 synthesizeToUri 条件下支持
        mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1");
        //	mTts.setParameter(SpeechConstant.TTS_BUFFER_TIME,"1");

        // 设置在线合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));

        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,
                Objects.requireNonNull(getExternalFilesDir("msc")).getAbsolutePath() + "/tts.pcm");
    }

    /**
     * 简化掉添加文件的方法~~
     * 2024年5月14日21:49:02
     */
    /*private void appendFile(File file, byte[] buffer) {
        try {
            if (!file.exists()) {
                boolean b = file.createNewFile();
            }
            RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
            randomFile.seek(file.length());
            randomFile.write(buffer);
            randomFile.close();
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }
    }*/

    @Override
    protected void onDestroy() {
        if (null != mTts) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(String msg) {

        // 根据播放进度来适应逻辑~~
        if (mPercentForPlaying > 0 && mPercentForPlaying < 90) {
            mTts.stopSpeaking();
            mPercentForPlaying = 0;
        } else {
            // pcmFile = new File(Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath(), "tts_pcmFile.pcm");
            // pcmFile.delete();
            // texts = ((TextView) findViewById(R.id.tts_text)).getText().toString();
            texts = msg;
            // 设置参数
            setParam();
            // 合成并播放
            int code = mTts.startSpeaking(texts, mTtsListener);

            if (code != ErrorCode.SUCCESS) {
                showTip("语音合成失败,错误码: " + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    }
}