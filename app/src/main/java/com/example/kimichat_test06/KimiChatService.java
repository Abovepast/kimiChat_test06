package com.example.kimichat_test06;

import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class KimiChatService {

    private JSONObject jsonRequest;
    private JSONArray parsedMessages;
    private final String API_KEY = "sk-sUSOaFGYu65PGuvQESpShXvXi1k73ZMrpAvCjnrxEDj5wQu3";

    public KimiChatService() {
        jsonRequest = initModelJSON();
        parsedMessages = jsonRequest.getJSONArray("messages");
    }

    public String sendRequestWithOkHttp(String userSend) throws JSONException, IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS) // 设置连接超时时间
                .readTimeout(20, TimeUnit.SECONDS) // 设置读取超时时间
                .build();
        saveUserSend(userSend);
        Request request = new Request.Builder()
                .url("https://api.moonshot.cn/v1/chat/completions")//需要请求的网址
                .post(RequestBody.Companion.create(jsonRequest.toString(), MediaType.parse("application/json; charset=utf-8")))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseString = response.body().string();
                Log.d("AAAA:response", responseString);
                JSONObject responseJSON = JSON.parseObject(responseString);
                JSONArray choicesArray = responseJSON.getJSONArray("choices");
                JSONObject choice = choicesArray.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                String content = message.getString("content");
                minusSend(content);
                return content;
            } else {
                return "Error! 回应解析失败!";
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return "Error! 请求超时!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error! 请求失败!\n错误信息:" + e;
        }
    }

    private void minusSend(String finalContent) {
        parsedMessages.remove(1);
        jsonRequest.remove("messages");
        jsonRequest.put("messages", parsedMessages);
        Log.d("AAAA:saveBotSend", jsonRequest.toString());
    }

    private void saveUserSend(String userSend) {
        JSONObject newMessage = new JSONObject();
        newMessage.put("role", "user");
        newMessage.put("content", userSend);
        parsedMessages.add(newMessage);
        jsonRequest.put("messages", parsedMessages);
        Log.d("AAAA:saveUserSend", jsonRequest.toString());
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
        system.put("content", "你是 KunKun，由 进进菜鸟AI 提供的人工智能助手，你更擅长中文和英文的对话。" +
                "你会为用户提供安全，有帮助，准确的回答。同时，你会拒绝一些涉及恐怖主义，种族歧视，黄色暴力等问题的回答。" +
                "进进菜鸟AI 为专有名词，不可翻译成其他语言。");
        messages.add(system);
        jsonRequest.put("messages", messages);
        jsonRequest.put("temperature", 0.3);

        return jsonRequest;
    }
}
