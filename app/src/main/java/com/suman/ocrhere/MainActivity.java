package com.suman.ocrhere;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button mBtn, mBtn1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtn = findViewById(R.id.btn);
        mBtn1 = findViewById(R.id.btn5);
        mBtn.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(i);
        });
        mBtn1.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, StorageActivity.class);
            startActivity(i);
        });
    }
}