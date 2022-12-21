package com.example.android_1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 查看获取 token 值
        SharedPreferences sp = getSharedPreferences("sp_city", MODE_PRIVATE);
        String token = sp.getString("token", "");
        Log.e("获取 toke 值 ---->", token);
    }
}
