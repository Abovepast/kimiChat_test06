package com.example.kimichat_test06;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
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
        Button getAKey = findViewById(R.id.getAKey);
//        Button getCurentKEY = findViewById(R.id.getCurentKEY);
        EditText edit_apikey = findViewById(R.id.edit_apikey);
        SharedPreferences sPGetApiKey = getSharedPreferences("api_key", MODE_PRIVATE);
        edit_apikey.setText(sPGetApiKey.getString("api_key", ""));

        back.setOnClickListener(v -> finish());
        textView8.setMovementMethod(LinkMovementMethod.getInstance());
        TextView goToAKey = findViewById(R.id.goToAKey);
        goToAKey.setMovementMethod(LinkMovementMethod.getInstance());
        sendApiKey.setOnClickListener(v -> {
            // 弹窗提醒确认
            // 创建AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("确认操作")
                    .setMessage("您确定要执行此操作吗？")
                    .setPositiveButton("确认", (dialog, which) -> {
                        // 用户点击了确认按钮
                        // 使用SharedPreferences将API_KEY保存起来
                        SharedPreferences sharedPreferences = getSharedPreferences("kimiChat", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("API_KEY", edit_apikey.getText().toString()).apply();
                        Toast.makeText(AboutActivity.this, "API_KEY设置成功！现在你可以和kunkun对话啦！", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        // 用户点击了取消按钮
                        Toast.makeText(AboutActivity.this, "取消操作！", Toast.LENGTH_SHORT).show();
                    }).create().show();

//            // 创建并显示对话框
//            AlertDialog dialog = builder.create();
//            dialog.show();

        });

        getAKey.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("kimiChat", MODE_PRIVATE);
            edit_apikey.setText(sharedPreferences.getString("API_KEY", ""));
            if (edit_apikey.getText().toString().equals("")) {
                Toast.makeText(AboutActivity.this, "本地没有储存你的API_KEY\n你可能需要先注册", Toast.LENGTH_SHORT).show();
            }
        });

//        getCurentKEY.setOnClickListener(v -> {
//            Toast.makeText(this, "当前API_KEY为：" + KimiChatService.API_KEY, Toast.LENGTH_SHORT).show();
//            edit_apikey.setText(KimiChatService.API_KEY);
//        });
    }
}