package com.example.catscanv4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button scan, upload, home, logs;

    int CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scan =  findViewById(R.id.btnScan);
        upload = (Button) findViewById(R.id.btnUpload);
        home = (Button) findViewById(R.id.btnHome);
        logs = (Button) findViewById(R.id.btnLogs);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MainActivity.this,ScanActivity.class);
                    startActivity(intent);
                }else{
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                }
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(),"Home Button working!", Toast.LENGTH_SHORT).show();
            }
        });

        logs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(),"Logs Button working!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestcode, String[] permissions, int[] grantresults){
        super.onRequestPermissionsResult(requestcode, permissions, grantresults);

        if(requestcode == CAMERA_PERMISSION_CODE){
            if(grantresults.length > 0 && grantresults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(MainActivity.this,ScanActivity.class);

                startActivity(intent);
            }else{
                Toast.makeText(getBaseContext(),"Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}