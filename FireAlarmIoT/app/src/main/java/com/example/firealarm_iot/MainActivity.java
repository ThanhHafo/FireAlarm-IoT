package com.example.firealarm_iot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView current;
    ImageView history;
    ImageView state;
    ImageView dataAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        current = findViewById(R.id.sensorData);
        history = findViewById(R.id.historyData);
        state = findViewById(R.id.state);
        dataAnalysis = findViewById(R.id.dataAnalysis);

        current.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, activity_current_environment.class);
            startActivity(intent);
        });

        history.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, history_environment.class);
            startActivity(intent);
        });

        state.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, control.class);
            startActivity(intent);
        });

        dataAnalysis.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, DataAnalysisActivity.class);
            startActivity(intent);
        });
    }

}