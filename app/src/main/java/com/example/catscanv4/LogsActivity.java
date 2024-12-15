package com.example.catscanv4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class LogsActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter adapter;

    Button home,upload,logs, btnReset;

    PredictionDB db;
    List list  = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_logs);
        db = new PredictionDB(this);

        listView = findViewById(R.id.listview);

        home = findViewById(R.id.btnHome);
        upload = findViewById(R.id.btnUpload);
        logs = findViewById(R.id.btnLogs);
        btnReset = findViewById(R.id.btnResetLogs);

        list = db.fetchDB();

        adapter = new ArrayAdapter(LogsActivity.this, R.layout.list_content,R.id.list_content, list);
        listView.setAdapter(adapter);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogsActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });

        logs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Toast.makeText(getBaseContext(),"Logs", Toast.LENGTH_SHORT).show();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.resetLogs();
                Intent intent = new Intent(LogsActivity.this, LogsActivity.class);
                startActivity(intent);
            }
        });

    }
}