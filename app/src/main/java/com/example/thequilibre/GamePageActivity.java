package com.example.thequilibre;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GamePageActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "com.example.thequilibre.extra.DIFFICULTY";
    public static final String DIFFICULTY_EASY = "Easy";
    public static final String DIFFICULTY_MEDIUM = "Medium";
    public static final String DIFFICULTY_HARD = "Hard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (gamePageRoot, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            gamePageRoot.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        if (difficulty == null) {
            difficulty = DIFFICULTY_MEDIUM;
        }
        applyDifficulty(difficulty);
    }

    private void applyDifficulty(String difficulty) {
        LinearLayout topDataZone = findViewById(R.id.top_data_zone);
        topDataZone.removeAllViews();

        TextView difficultyLabel = new TextView(this);
        difficultyLabel.setText(getString(R.string.selected_difficulty_label, difficulty));
        difficultyLabel.setTextSize(22f);
        difficultyLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        difficultyLabel.setPadding(0, 24, 0, 24);
        topDataZone.addView(difficultyLabel);

        TextView square21 = findViewById(R.id.square_2_1);
        TextView square22 = findViewById(R.id.square_2_2);
        TextView square23 = findViewById(R.id.square_2_3);
        TextView square11 = findViewById(R.id.square_1_1);
        TextView square12 = findViewById(R.id.square_1_2);
        TextView square13 = findViewById(R.id.square_1_3);
        TextView[] allSquares = {square11, square12, square13, square21, square22, square23};

        if (DIFFICULTY_EASY.equals(difficulty)) {
            square21.setVisibility(View.GONE);
            square22.setVisibility(View.GONE);
            square23.setVisibility(View.GONE);
            applySquareAlpha(allSquares, 0.75f);
        } else if (DIFFICULTY_HARD.equals(difficulty)) {
            square21.setVisibility(View.VISIBLE);
            square22.setVisibility(View.VISIBLE);
            square23.setVisibility(View.VISIBLE);
            applySquareAlpha(allSquares, 1f);
        } else {
            square21.setVisibility(View.VISIBLE);
            square22.setVisibility(View.VISIBLE);
            square23.setVisibility(View.VISIBLE);
            applySquareAlpha(allSquares, 0.9f);
        }
    }

    private void applySquareAlpha(TextView[] squares, float alpha) {
        for (TextView square : squares) {
            square.setAlpha(alpha);
        }
    }
}
