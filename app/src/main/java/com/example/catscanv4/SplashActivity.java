package com.example.catscanv4;

import android.content.Intent;
import android.os.Bundle;
import android.telecom.Call;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    Intent CallMain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        CallMain = new Intent(SplashActivity.this,MainActivity.class);

        Thread t = new Thread(){
            public void run(){
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }finally {
                    startActivity(CallMain);
                    finish();
                }
            }
        };
        t.start();
    }
}