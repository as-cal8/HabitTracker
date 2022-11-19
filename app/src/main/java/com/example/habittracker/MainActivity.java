package com.example.habittracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private int value = 0;
    private int current_value = 0;
    private Button refresh_button;
    private Button connect_button;
    private TextView current_view;
    private TextView max_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.refresh_button = findViewById(R.id.refresh);
        this.connect_button = findViewById(R.id.btConnect);
        this.current_view = findViewById(R.id.currentWinstreakValue);
        this.max_view = findViewById(R.id.maxWinstreakValue);

        this.refresh_button.setOnClickListener(this::refresh);
        this.connect_button.setOnClickListener(this::btConnect);
    }

    public void btConnect(View view) {
        // TODO connect to bluetooth here

    }

    @SuppressLint("DefaultLocale")
    public void refresh(View view) {
        this.value++;
        this.value++;
        this.current_value++;
        this.current_view.setText(String.format("%d",this.current_value));
        this.max_view.setText(String.format("%d",this.value));
    }
}