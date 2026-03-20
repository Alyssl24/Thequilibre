package com.example.thequilibre;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class HomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        Button playButton = findViewById(R.id.button_play);
        playButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomePageActivity.this, GamePageActivity.class);
            startActivity(intent);
        });
    }
}
