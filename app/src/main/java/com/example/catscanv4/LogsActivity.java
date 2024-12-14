package com.example.catscanv4;

import android.os.Bundle;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

    PredictionDB db;
    List list  = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_logs);
        db = new PredictionDB(this);

        listView = findViewById(R.id.listview);

        list = db.fetchDB();

        adapter = new ArrayAdapter(LogsActivity.this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

    }
}