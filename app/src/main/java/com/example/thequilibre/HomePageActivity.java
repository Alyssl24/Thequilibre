package com.example.thequilibre;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;

public class HomePageActivity extends AppCompatActivity {

    private String selectedDifficulty = GamePageActivity.DIFFICULTY_MEDIUM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        android.view.View homeRoot = findViewById(R.id.home_page);
        int initialPaddingLeft = homeRoot.getPaddingLeft();
        int initialPaddingTop = homeRoot.getPaddingTop();
        int initialPaddingRight = homeRoot.getPaddingRight();
        int initialPaddingBottom = homeRoot.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(homeRoot, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    initialPaddingLeft + systemBars.left,
                    initialPaddingTop + systemBars.top,
                    initialPaddingRight + systemBars.right,
                    initialPaddingBottom + systemBars.bottom
            );
            return insets;
        });

        TextView aboutLink = findViewById(R.id.text_about_link);
        aboutLink.setOnClickListener(view -> {
            Intent intent = new Intent(HomePageActivity.this, AboutUsActivity.class);
            startActivity(intent);
        });

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
