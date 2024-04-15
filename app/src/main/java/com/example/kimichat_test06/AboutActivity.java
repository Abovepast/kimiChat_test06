package com.example.kimichat_test06;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageView back = findViewById(R.id.back_to_main);
        TextView textView8 = findViewById(R.id.textView8);
        Button sendApiKey = findViewById(R.id.sendApiKey);
        //Button getAKey = findViewById(R.id.getAKey);
//        Button getCurrentKEY = findViewById(R.id.getCurrentKEY);
        EditText edit_apikey = findViewById(R.id.edit_apikey);
        SharedPreferences sPGetApiKey = getSharedPreferences("api_key", MODE_PRIVATE);
        edit_apikey.setText(sPGetApiKey.getString("api_key", ""));

        back.setOnClickListener(v -> finish());
        textView8.setMovementMethod(LinkMovementMethod.getInstance());
        TextView goToAKey = findViewById(R.id.goToAKey);
        goToAKey.setMovementMethod(LinkMovementMethod.getInstance());

        // 显示当前设置的API_KEY
        SharedPreferences spf = getSharedPreferences("kimiChat", MODE_PRIVATE);
        edit_apikey.setText(spf.getString("API_KEY", ""));

        // 设置并保存API_KEY
        sendApiKey.setOnClickListener(v -> {
            // 弹窗提醒确认
            // 创建AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final EditText input = new EditText(this);
            builder.setTitle("设置API_KEY")
                    .setView(input)
                    .setPositiveButton("确认", (dialog, which) -> {
                        // 用户点击了确认按钮
                        // 使用SharedPreferences将API_KEY保存起来
                        SharedPreferences sharedPreferences = getSharedPreferences("kimiChat", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("API_KEY", input.getText().toString()).apply();
                        Toast.makeText(AboutActivity.this, "API_KEY设置成功！\n请回到主页点击'重置对话'!", Toast.LENGTH_SHORT).show();
                        edit_apikey.setText(sharedPreferences.getString("api_key", ""));
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        // 用户点击了取消按钮
                        Toast.makeText(AboutActivity.this, "取消操作！", Toast.LENGTH_SHORT).show();
                    }).create().show();
        });

        sendApiKey.setOnLongClickListener(v->{
            /*ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("selected_names", edit_apikey.getText().toString());
            if (clipboardManager != null) {
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(this, "API_KEY已复制到剪贴板, 请妥善保存。", Toast.LENGTH_SHORT).show();
            }*/
            SharedPreferences sharedPreferences = getSharedPreferences("kimiChat", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String authorAK = "sk-y8y4Dz5zCzj85h3vCIP3SJFTjj5a6xznpicYeD2gKXs3pIem";
            editor.putString("API_KEY", authorAK).apply();
            Toast.makeText(AboutActivity.this, "已切换到作者的API_KEY", Toast.LENGTH_SHORT).show();
            edit_apikey.setText(sharedPreferences.getString("API_KEY", ""));
            return true;
        });

        // 获取本地保存的API_KEY
        /*getAKey.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("kimiChat", MODE_PRIVATE);
            edit_apikey.setText(sharedPreferences.getString("API_KEY", ""));
            if (edit_apikey.getText().toString().isEmpty()) {
                Toast.makeText(AboutActivity.this, "本地没有储存你的API_KEY\n你可能需要先注册", Toast.LENGTH_SHORT).show();
            }
        });*/

    }
}