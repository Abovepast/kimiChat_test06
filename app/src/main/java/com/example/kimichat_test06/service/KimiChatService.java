package com.example.kimichat_test06.service;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KimiChatService {

    private final JSONObject jsonRequest;
    private final JSONArray parsedMessages;
    private final OkHttpClient client;
//    private static final String API_KEY = "";

    public KimiChatService() {
        jsonRequest = initModelJSON();
        parsedMessages = jsonRequest.getJSONArray("messages");
        client = getOkHttpClient();
    }

    public KimiChatService(boolean isEnglish) {
        if (isEnglish) {
            Toast.makeText(null, "已切换到英文模式", Toast.LENGTH_SHORT).show();
        }
        jsonRequest = initModelJSON();
        parsedMessages = jsonRequest.getJSONArray("messages");
        client = getOkHttpClient();
        String english_learn_model = "接下来我将会连续输入英文句子，你将分析句子成分，分析结果带中文翻译。";
        saveUserSend(english_learn_model);
    }

    public String sendRequestWithOkHttp(String userSend, String API_KEY) throws JSONException, IOException {

        saveUserSend(userSend);
        Request request = new Request.Builder()
                .url("https://api.moonshot.cn/v1/chat/completions")//需要请求的网址
                .post(RequestBody.Companion.create(jsonRequest.toString(), MediaType.parse("application/json; charset=utf-8")))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();
//        Log.d("AAAA:请求发送成功！", request.toString());
        try (Response response = client.newCall(request).execute()) {
//            Log.d("AAAA:请求接收成功！", response.toString());
            if (response.isSuccessful() && response.body() != null) {
                String responseString = response.body().string();
//                Log.d("AAAA:responseString", responseString);
                JSONObject responseJSON = JSON.parseObject(responseString);
                JSONArray choicesArray = responseJSON.getJSONArray("choices");
                JSONObject choice = choicesArray.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                String content = message.getString("content");
                minusSend();
                return content;
            } else if (API_KEY.isEmpty()) {
                return "请先设置API_KEY!";
            } else if(response.code() == 401) {
                return "Error! API_KEY错误!";
            } else if(response.code() == 400) {
                return "Error! 请求参数错误!";
            } else if(response.code() == 429) {
                return "Error! 请求频率过高!\n" +
                        "建议降低temperature参数或等待1分钟再试。\n" +
                        "如果持续出现此错误，请检查API_KEY是否正确。";
            } else if(response.code() != 200) {
                return "Error! 错误代码:" + response.code() + "\n" +
                        "错误信息:" + response.message() + "\n" +
                        "主体信息:" + response.body();
            } else {
                return "Error! 未知错误!";
            }
        } catch (SocketTimeoutException e) {
            return "Error! 请求超时!\n" + e;
        } catch (IOException e) {
            return "Error! 请求失败!\n错误信息:" + e;
        }
    }

    @NonNull
    private static OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
                .connectTimeout(40, TimeUnit.SECONDS) // 设置连接超时时间
                .readTimeout(40, TimeUnit.SECONDS) // 设置读取超时时间
                .build();
    }


    //
    private void minusSend() {
        // 当列表中消息数大于5，删除消息数组中的第2条
        if (parsedMessages.size() >5 ) {
            parsedMessages.remove(1);
            jsonRequest.remove("messages");
            jsonRequest.put("messages", parsedMessages);
        }
    }

    // 保存用户对话内容，用于连续对话
    private void saveUserSend(String userSend) {
        JSONObject newMessage = new JSONObject();
        newMessage.put("role", "user");
        newMessage.put("content", userSend);
        parsedMessages.add(newMessage);
        jsonRequest.put("messages", parsedMessages);
    }

    @NonNull
    public static JSONObject initModelJSON() throws JSONException {
        // 创建JSON请求体
        JSONObject jsonRequest = new JSONObject();
        // 添加模型信息
        jsonRequest.put("model", "moonshot-v1-32k");
        // 添加消息数组
        JSONArray messages = new JSONArray();
        JSONObject system = new JSONObject();
        system.put("role", "system");
        system.put("content", "你是 KunKun，由 进进菜鸟 提供的人工智能助手，你更擅长中文和英文的对话。" +
                "你会为用户提供安全，有帮助，准确的回答。同时，你会拒绝一些涉及恐怖主义，种族歧视，黄色暴力等问题的回答。" +
                "进进菜鸟 为专有名词，不可翻译成其他语言。");
        messages.add(system);
        jsonRequest.put("messages", messages);
        jsonRequest.put("temperature", 0.3);
//        jsonRequest.put("stream", true);
        return jsonRequest;
    }
}
