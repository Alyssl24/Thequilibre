package com.example.thequilibre;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.thequilibre.game.ObstacleSystem;
import com.example.thequilibre.history.ScoreHistoryRepository;
import com.example.thequilibre.model.GameView;

import java.util.Arrays;
import java.util.List;

public class GamePageActivity extends AppCompatActivity {

    public static final String DIFFICULTY_MEDIUM = "Medium";
    public static final String DIFFICULTY_EASY = "Easy";
    public static final String DIFFICULTY_HARD = "Hard";
    public static final String EXTRA_DIFFICULTY = "com.example.thequilibre.extra.DIFFICULTY";
    public static final String EXTRA_FINAL_SCORE = "com.example.thequilibre.extra.FINAL_SCORE";
    public static final String EXTRA_GAME_OVER_REASON = "com.example.thequilibre.extra.GAME_OVER_REASON";
    public static final String EXTRA_FINAL_DIFFICULTY = "com.example.thequilibre.extra.FINAL_DIFFICULTY";

    private static final float ROTATION_SMOOTHING = 0.18f;
    private static final float MAX_BATON_ROTATION_DEGREES = 50f;
    private static final float GAME_OVER_TEMPERATURE_CELSIUS = 60f;
    private static final int SAMPLE_RATE = 8000;
    private static final int POINTS_PER_VALIDATED_SQUARE = 3;

    private enum GameOverReason {
        CUP_COLLISION,
        TEMPERATURE_LIMIT
    }

    private GameView gameView;
    private BatonView batonView;
    private CupView cupView;
    private ObstacleSystem obstacleSystem;
    private ScoreHistoryRepository scoreHistoryRepository;

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private int bufferSize;

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;

    private final float[] rotationMatrix = new float[9];
    private final float[] remappedRotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private float filteredRotationDegrees;
    private boolean batonStartPositionInitialized;
    private int currentScore;
    private int finalScore;
    private boolean isGameOver;
    private GameOverReason gameOverReason;
    private String selectedDifficultyForRun = DIFFICULTY_MEDIUM;
    private boolean hasLaunchedEndScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_page);

        FrameLayout container = findViewById(R.id.game_container);
        FrameLayout obstacleOverlay = findViewById(R.id.obstacle_overlay);
        View root = findViewById(R.id.main);

        batonView = findViewById(R.id.baton_view);
        cupView = findViewById(R.id.cup_view);
        View space1 = findViewById(R.id.space_1);
        View space2 = findViewById(R.id.space_2);
        View referenceSquare = findViewById(R.id.square_1_1);

        if (cupView != null) {
            cupView.attachToBaton(batonView);
        }
        batonView.setStateListener(() -> {
            if (cupView != null) {
                cupView.invalidate();
            }
        });

        gameView = new GameView(this);
        scoreHistoryRepository = new ScoreHistoryRepository(this);
        currentScore = 0;
        finalScore = 0;
        isGameOver = false;
        gameOverReason = null;
        hasLaunchedEndScreen = false;
        gameView.setScore(currentScore);
        gameView.setGameEventListener(temperatureCelsius -> runOnUiThread(() -> {
            if (temperatureCelsius >= GAME_OVER_TEMPERATURE_CELSIUS) {
                triggerGameOver(GameOverReason.TEMPERATURE_LIMIT);
            }
        }));

        String difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);

        if (difficulty == null) {
            difficulty = DIFFICULTY_MEDIUM;
        }
        gameView.setDifficulty(difficulty);

        container.addView(gameView);

        List<View> slotViews = Arrays.asList(
                findViewById(R.id.square_1_1),
                findViewById(R.id.square_1_2),
                findViewById(R.id.square_1_3),
                findViewById(R.id.square_2_1),
                findViewById(R.id.square_2_2),
                findViewById(R.id.square_2_3)
        );

        String selectedDifficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        if (selectedDifficulty == null || selectedDifficulty.trim().isEmpty()) {
            selectedDifficulty = DIFFICULTY_MEDIUM;
        }
        selectedDifficultyForRun = selectedDifficulty;

        obstacleSystem = new ObstacleSystem(
                this,
                obstacleOverlay,
                slotViews,
                batonView,
                selectedDifficulty,
                new ObstacleSystem.Listener() {
                    @Override
                    public void onDangerousCollision(int slotIndex) {
                        batonView.notifyDangerCollision();
                        if (cupView != null) {
                            cupView.invalidate();
                        }
                        triggerGameOver(GameOverReason.CUP_COLLISION);
                    }

                    @Override
                    public void onObstacleValidated(int slotIndex) {
                        handleValidatedSquare(slotIndex);
                    }
                }
        );

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        ViewCompat.setOnApplyWindowInsetsListener(root, (gamePageRoot, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            gamePageRoot.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            gamePageRoot.post(() -> {
                configureBatonBounds(batonView, space1, space2, referenceSquare);
                if (obstacleSystem != null) {
                    obstacleSystem.updateLayoutBounds();
                }
            });

            return insets;
        });

        root.addOnLayoutChangeListener((v, left, top, right, bottom,
                                        oldLeft, oldTop, oldRight, oldBottom) -> {
            configureBatonBounds(batonView, space1, space2, referenceSquare);
            if (obstacleSystem != null) {
                obstacleSystem.updateLayoutBounds();
            }
        });

        root.post(() -> {
            configureBatonBounds(batonView, space1, space2, referenceSquare);
            if (obstacleSystem != null) {
                obstacleSystem.updateLayoutBounds();
            }
        });

        checkAudioPermission();
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1
            );
        } else {
            startMicro();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMicro();
            }
        }
    }

    private void startMicro() {
        if (isGameOver) {
            return;
        }
        bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );

        audioRecord.startRecording();
        isRecording = true;

        startListening();
    }

    private void startListening() {
        new Thread(() -> {
            short[] buffer = new short[bufferSize];

            while (isRecording) {
                int read = audioRecord.read(buffer, 0, bufferSize);

                long sum = 0;
                for (int i = 0; i < read; i++) {
                    sum += Math.abs(buffer[i]);
                }

                int micLevel = (int) (sum / (read + 1));
                boolean isBlowing = micLevel > 5000;

                runOnUiThread(() -> gameView.setBlowing(isBlowing));
            }
        }).start();
    }

    private void handleValidatedSquare(int slotIndex) {
        if (isGameOver) {
            return;
        }
        addScore(POINTS_PER_VALIDATED_SQUARE);
    }

    private void addScore(int points) {
        currentScore += Math.max(0, points);
        if (gameView != null) {
            gameView.setScore(currentScore);
        }
    }

    private void triggerGameOver(GameOverReason reason) {
        if (isGameOver) {
            return;
        }
        isGameOver = true;
        gameOverReason = reason;
        finalScore = currentScore;
        if (scoreHistoryRepository != null) {
            scoreHistoryRepository.saveScore(finalScore, selectedDifficultyForRun);
        }

        if (gameView != null) {
            gameView.setGameOver(true);
            gameView.setBlowing(false);
        }
        if (obstacleSystem != null) {
            obstacleSystem.pause();
            batonView.post(() -> {
                if (obstacleSystem != null) {
                    obstacleSystem.stop();
                }
            });
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(orientationListener);
        }
        stopMicrophoneCapture();
        navigateToEndScreen();
    }

    private void navigateToEndScreen() {
        if (hasLaunchedEndScreen || isFinishing() || isDestroyed()) {
            return;
        }
        hasLaunchedEndScreen = true;

        Intent intent = EndGameActivity.createIntent(
                this,
                finalScore,
                selectedDifficultyForRun,
                gameOverReason != null ? gameOverReason.name() : null
        );
        startActivity(intent);
        finish();
    }

    private void stopMicrophoneCapture() {
        isRecording = false;
        if (audioRecord == null) {
            return;
        }
        try {
            audioRecord.stop();
        } catch (IllegalStateException ignored) {
            // Recorder already stopped or not initialized.
        }
        audioRecord.release();
        audioRecord = null;
    }

    public Bundle buildGameOverPayloadForEndScreen() {
        Bundle payload = new Bundle();
        payload.putInt(EXTRA_FINAL_SCORE, finalScore);
        payload.putString(EXTRA_FINAL_DIFFICULTY, selectedDifficultyForRun);
        if (gameOverReason != null) {
            payload.putString(EXTRA_GAME_OVER_REASON, gameOverReason.name());
        }
        return payload;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isGameOver && sensorManager != null && rotationVectorSensor != null) {
            sensorManager.registerListener(
                    orientationListener,
                    rotationVectorSensor,
                    SensorManager.SENSOR_DELAY_GAME
            );
        }

        if (!isGameOver && obstacleSystem != null) {
            obstacleSystem.updateLayoutBounds();
            obstacleSystem.resumeOrStart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(orientationListener);
        }

        if (obstacleSystem != null) {
            obstacleSystem.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (obstacleSystem != null) {
            obstacleSystem.stop();
        }

        stopMicrophoneCapture();
    }

    private void configureBatonBounds(BatonView batonView, View space1, View space2, View referenceSquare) {
        float ySpace1 = getCenterYInViewCoordinates(space1, batonView);
        float ySpace2 = getCenterYInViewCoordinates(space2, batonView);

        float minY = Math.min(ySpace1, ySpace2);
        float maxY = Math.max(ySpace1, ySpace2);

        batonView.setMovementBounds(minY, maxY);
        batonView.setCupSizeFromSquare(referenceSquare.getWidth(), referenceSquare.getHeight());
        if (!batonStartPositionInitialized) {
            batonView.moveTo(ySpace1);
            batonStartPositionInitialized = true;
        }
        if (cupView != null) {
            cupView.invalidate();
        }
    }

    private float getCenterYInViewCoordinates(View source, View target) {
        int[] sourceLocation = new int[2];
        int[] targetLocation = new int[2];
        source.getLocationOnScreen(sourceLocation);
        target.getLocationOnScreen(targetLocation);
        return (sourceLocation[1] - targetLocation[1]) + (source.getHeight() / 2f);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private final SensorEventListener orientationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR || batonView == null) {
                return;
            }

            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            int axisX = SensorManager.AXIS_X;
            int axisY = SensorManager.AXIS_Y;

            int screenRotation = getWindowManager().getDefaultDisplay().getRotation();

            if (screenRotation == Surface.ROTATION_90) {
                axisX = SensorManager.AXIS_Y;
                axisY = SensorManager.AXIS_MINUS_X;
            } else if (screenRotation == Surface.ROTATION_180) {
                axisX = SensorManager.AXIS_MINUS_X;
                axisY = SensorManager.AXIS_MINUS_Y;
            } else if (screenRotation == Surface.ROTATION_270) {
                axisX = SensorManager.AXIS_MINUS_Y;
                axisY = SensorManager.AXIS_X;
            }

            SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, remappedRotationMatrix);
            SensorManager.getOrientation(remappedRotationMatrix, orientationAngles);

            float rollDegrees = (float) Math.toDegrees(orientationAngles[2]);

            float clampedRotation = clamp(rollDegrees, -MAX_BATON_ROTATION_DEGREES, MAX_BATON_ROTATION_DEGREES);

            filteredRotationDegrees += ROTATION_SMOOTHING * (clampedRotation - filteredRotationDegrees);

            batonView.setBatonRotationDegrees(filteredRotationDegrees);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
}
