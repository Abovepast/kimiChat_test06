package com.example.kimichat_test06;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageView back = findViewById(R.id.back_to_main);

        back.setOnClickListener(v -> finish());

        TextView textView8 = findViewById(R.id.textView8);
        textView8.setMovementMethod(LinkMovementMethod.getInstance());
    }
}