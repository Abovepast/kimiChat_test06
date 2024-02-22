package com.example.kimichat_test06;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class SettingAndAbout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_and_about);

        ImageView back = findViewById(R.id.back_to_main);

        back.setOnClickListener(v -> {
            finish();
        });
    }
}