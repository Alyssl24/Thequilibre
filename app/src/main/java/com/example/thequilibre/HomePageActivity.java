package com.example.thequilibre;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;

public class HomePageActivity extends AppCompatActivity {

    private String selectedDifficulty = GamePageActivity.DIFFICULTY_MEDIUM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        MaterialButtonToggleGroup difficultyToggleGroup = findViewById(R.id.difficulty_toggle_group);
        difficultyToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.button_difficulty_easy) {
                selectedDifficulty = GamePageActivity.DIFFICULTY_EASY;
            } else if (checkedId == R.id.button_difficulty_hard) {
                selectedDifficulty = GamePageActivity.DIFFICULTY_HARD;
            } else {
                selectedDifficulty = GamePageActivity.DIFFICULTY_MEDIUM;
            }
        });

        Button playButton = findViewById(R.id.button_play);
        playButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomePageActivity.this, GamePageActivity.class);
            intent.putExtra(GamePageActivity.EXTRA_DIFFICULTY, selectedDifficulty);
            startActivity(intent);
        });
    }
}
