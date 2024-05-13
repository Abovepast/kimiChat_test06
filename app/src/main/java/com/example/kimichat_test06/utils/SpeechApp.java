package com.example.kimichat_test06.utils;

import android.app.Application;
import android.content.Context;

import com.example.kimichat_test06.R;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechUtility;

public class SpeechApp extends Application {

    public static final String PRIVACY_KEY = "privacy_key";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private static boolean mscInitialize = false;

    public static void initializeMsc(Context context){
        if (mscInitialize) return;
        // 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用半角“,”分隔。
        // 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符

        // 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误

        SpeechUtility.createUtility(context, "appid=" + context.getString(R.string.app_id));

        // 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
        Setting.setShowLog(true);
        mscInitialize = true;
    }

}
