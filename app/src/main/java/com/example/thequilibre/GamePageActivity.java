package com.example.thequilibre;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GamePageActivity extends AppCompatActivity {

    public static final String DIFFICULTY_MEDIUM = "Medium";
    public static final String DIFFICULTY_EASY = "Easy";
    public static final String DIFFICULTY_HARD = "Hard";
    public static final String EXTRA_DIFFICULTY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_page);

        View root = findViewById(R.id.main);
        BatonView batonView = findViewById(R.id.baton_view);
        View space1 = findViewById(R.id.space_1);
        View space2 = findViewById(R.id.space_2);

        ViewCompat.setOnApplyWindowInsetsListener(root, (gamePageRoot, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            gamePageRoot.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            gamePageRoot.post(() -> configureBatonBounds(batonView, space1, space2));
            return insets;
        });

        root.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                configureBatonBounds(batonView, space1, space2));
    }

    private void configureBatonBounds(BatonView batonView, View space1, View space2) {
        float ySpace1 = getCenterYInViewCoordinates(space1, batonView);
        float ySpace2 = getCenterYInViewCoordinates(space2, batonView);

        float minY = Math.min(ySpace1, ySpace2);
        float maxY = Math.max(ySpace1, ySpace2);

        batonView.setMovementBounds(minY, maxY);
        batonView.moveTo(ySpace1);
    }

    private float getCenterYInViewCoordinates(View source, View target) {
        int[] sourceLocation = new int[2];
        int[] targetLocation = new int[2];
        source.getLocationOnScreen(sourceLocation);
        target.getLocationOnScreen(targetLocation);
        return (sourceLocation[1] - targetLocation[1]) + (source.getHeight() / 2f);
    }
}
