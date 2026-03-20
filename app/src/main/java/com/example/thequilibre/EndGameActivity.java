package com.example.thequilibre;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class EndGameActivity extends AppCompatActivity {

    private static final String GAME_OVER_REASON_COLLISION = "CUP_COLLISION";
    private static final String GAME_OVER_REASON_TEMPERATURE = "TEMPERATURE_LIMIT";

    @NonNull
    public static Intent createIntent(@NonNull Context context,
                                      int finalScore,
                                      @Nullable String difficulty,
                                      @Nullable String gameOverReason) {
        Intent intent = new Intent(context, EndGameActivity.class);
        intent.putExtra(GamePageActivity.EXTRA_FINAL_SCORE, Math.max(0, finalScore));
        if (difficulty != null) {
            intent.putExtra(GamePageActivity.EXTRA_FINAL_DIFFICULTY, difficulty);
        }
        if (gameOverReason != null) {
            intent.putExtra(GamePageActivity.EXTRA_GAME_OVER_REASON, gameOverReason);
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_end_game);

        View root = findViewById(R.id.end_game_root);
        int initialPaddingLeft = root.getPaddingLeft();
        int initialPaddingTop = root.getPaddingTop();
        int initialPaddingRight = root.getPaddingRight();
        int initialPaddingBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    initialPaddingLeft + systemBars.left,
                    initialPaddingTop + systemBars.top,
                    initialPaddingRight + systemBars.right,
                    initialPaddingBottom + systemBars.bottom
            );
            return insets;
        });

        int finalScore = Math.max(0, getIntent().getIntExtra(GamePageActivity.EXTRA_FINAL_SCORE, 0));
        String difficulty = normalizeDifficulty(
                getIntent().getStringExtra(GamePageActivity.EXTRA_FINAL_DIFFICULTY)
        );
        String reason = getIntent().getStringExtra(GamePageActivity.EXTRA_GAME_OVER_REASON);

        TextView scoreText = findViewById(R.id.end_game_score_value);
        TextView difficultyText = findViewById(R.id.end_game_difficulty_value);
        TextView reasonChip = findViewById(R.id.end_game_reason_chip);
        MaterialButton restartButton = findViewById(R.id.end_game_restart_button);
        MaterialButton homeButton = findViewById(R.id.end_game_home_button);

        scoreText.setText(getString(R.string.end_game_score_value, finalScore));
        difficultyText.setText(getString(R.string.end_game_difficulty_label, difficulty));
        bindReason(reasonChip, reason);

        restartButton.setOnClickListener(view -> restartGame(difficulty));
        homeButton.setOnClickListener(view -> navigateHome());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateHome();
            }
        });
    }

    private void bindReason(@NonNull TextView reasonChip, @Nullable String reason) {
        if (GAME_OVER_REASON_COLLISION.equals(reason)) {
            reasonChip.setText(R.string.end_game_reason_collision);
            reasonChip.setBackgroundResource(R.drawable.bg_end_reason_collision);
            reasonChip.setTextColor(0xFF7A302A);
            return;
        }
        if (GAME_OVER_REASON_TEMPERATURE.equals(reason)) {
            reasonChip.setText(R.string.end_game_reason_temperature);
            reasonChip.setBackgroundResource(R.drawable.bg_end_reason_temperature);
            reasonChip.setTextColor(0xFF6E3E08);
            return;
        }

        reasonChip.setText(R.string.end_game_reason_unknown);
        reasonChip.setBackgroundResource(R.drawable.bg_end_reason_neutral);
        reasonChip.setTextColor(0xFF4E3E67);
    }

    private void restartGame(@NonNull String difficulty) {
        Intent restartIntent = new Intent(this, GamePageActivity.class);
        restartIntent.putExtra(GamePageActivity.EXTRA_DIFFICULTY, difficulty);
        startActivity(restartIntent);
        finish();
    }

    private void navigateHome() {
        Intent homeIntent = new Intent(this, HomePageActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(homeIntent);
        finish();
    }

    @NonNull
    private String normalizeDifficulty(@Nullable String difficulty) {
        if (GamePageActivity.DIFFICULTY_EASY.equals(difficulty)) {
            return GamePageActivity.DIFFICULTY_EASY;
        }
        if (GamePageActivity.DIFFICULTY_HARD.equals(difficulty)) {
            return GamePageActivity.DIFFICULTY_HARD;
        }
        return GamePageActivity.DIFFICULTY_MEDIUM;
    }
}
