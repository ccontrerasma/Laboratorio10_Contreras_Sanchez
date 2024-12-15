package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar botones de control
        findViewById(R.id.btnStart).setOnClickListener(v -> sendActionToService("start"));
        findViewById(R.id.btnPause).setOnClickListener(v -> sendActionToService("pause"));
        findViewById(R.id.btnResume).setOnClickListener(v -> sendActionToService("resume"));
        findViewById(R.id.btnStop).setOnClickListener(v -> sendActionToService("stop"));
    }

    private void sendActionToService(String action) {
        Intent serviceIntent = new Intent(this, AudioPlayerService.class);
        serviceIntent.setAction(action);
        startService(serviceIntent);
    }
}
