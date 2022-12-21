package com.example.android_1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    // 各个组件类型
    private TextView txtUser, txtPwd;
    private Button btnLogin, btnReset;
    public Context mContext;

    // 启动执行的函数，相当于主函数
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        initView();
        initListener();
    }

    // 获取对应组件
    private void initView() {
        txtUser = findViewById(R.id.txt_user);
        txtPwd = findViewById(R.id.txt_pwd);

        btnLogin = findViewById(R.id.btn_login);
        btnReset = findViewById(R.id.btn_reset);
    }

    // 初始化按钮
    private void initListener() {
        // 登录按钮
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginForm();
            }
        });

        // 重置按钮
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("日志", "重置");
                txtUser.setText("");
                txtPwd.setText("");
            }
        });
    }

    // 登录按钮执行的函数
    private void LoginForm() {
        Log.e("日志", "登录");

        // 用于存储用户信息
        // 效果：{username:"123456", password:"666666"}
        TreeMap<String, String> dataMap = new TreeMap<String, String>();

        // 获取用户输入的用户名和密码
        HashMap<String, TextView> objMap = new HashMap<String, TextView>();
        objMap.put("username", txtUser);
        objMap.put("password", txtPwd);
        for (Map.Entry<String, TextView> entry : objMap.entrySet()) {
            String key = entry.getKey();
            TextView obj = entry.getValue();
            String value = String.valueOf(obj.getText());
            dataMap.put(key, value);
        }

        // 用于校验用户输入信息
        // 效果：password666666username123456
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key);
            sb.append(value);
        }
        String dataString = sb.toString();
        String signString = md5(dataString);
        // dataMap = {username:"123456", password:"666666", sign:"5a231fcdb710d73268c4f44283487ba2"}
        dataMap.put("sign", signString);

        // 使用线程，发送 HTTP 网络请求
        new Thread() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().build();
                // 发送 post 请求
                FormBody form = new FormBody.Builder()
                        .add("user", dataMap.get("username"))
                        .add("pwd", dataMap.get("password"))
                        .add("sign", dataMap.get("sign"))
                        .build();
                // 请求网址为测试用本地 FLASK 服务器 IP
                Request req = new Request.Builder().url("http://10.10.10.111:5000/auth").post(form).build();
                Call call = client.newCall(req);
                try {
                    Response res = call.execute();
                    ResponseBody body = res.body();
                    String bodyString = body.string();
                    // 接收示例：{"status":true, "token":"b96efd24-e323-4efd-8813-659570619cde"}

                    // 1. 获取登录状态 + token
                    HttpResponse obj = new Gson().fromJson(bodyString, HttpResponse.class);

                    // 2. token 保存手机 -> 本地 XML 文件（登录凭证保存到 cookie）
                    SharedPreferences sp = getSharedPreferences("sp_city", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("token", obj.token);
                    editor.commit();

                    // 3. 验证成功，跳转到新页面
                    Intent in = new Intent(mContext, Home.class);
                    startActivity(in);

                    Log.e("获取相应的内容 -->", bodyString);
                } catch (IOException ex) {
                    Log.e("请求异常 -->", "网络错误");
                }

            }
        }.start();
    }

    // MD5 加密函数
    private String md5(String dataString) {
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            byte[] nameBytes = instance.digest(dataString.getBytes());

            // 十六进制展示
            StringBuilder sb = new StringBuilder();
            for (byte nameByte : nameBytes) {
                int val = nameByte & 255; // 负数转换为正数
                if (val < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(val));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }

    }
}