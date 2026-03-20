package com.example.thequilibre;

import android.Manifest;
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

import com.example.thequilibre.model.GameView;

public class GamePageActivity extends AppCompatActivity {

    public static final String DIFFICULTY_MEDIUM = "";
    public static final String DIFFICULTY_EASY = "";
    public static final String DIFFICULTY_HARD = "";
    public static final String EXTRA_DIFFICULTY = "";

    private GameView gameView;

    private AudioRecord audioRecord;
    private boolean isRecording = false;

    private static final int SAMPLE_RATE = 8000;
    private int bufferSize;

    private BatonView batonView;
    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;

    private final float[] rotationMatrix = new float[9];
    private final float[] remappedRotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private float filteredRotationDegrees;

    private static final float ROTATION_SMOOTHING = 0.18f;
    private static final float MAX_BATON_ROTATION_DEGREES = 50f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_page);

        // 🎮 GameView
        FrameLayout container = findViewById(R.id.game_container);
        gameView = new GameView(this);
        container.addView(gameView);

        // 🎮 UI
        View root = findViewById(R.id.main);
        batonView = findViewById(R.id.baton_view);
        View space1 = findViewById(R.id.space_1);
        View space2 = findViewById(R.id.space_2);
        View referenceSquare = findViewById(R.id.square_1_1);

        // 🎮 Sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        // 🎮 Insets
        ViewCompat.setOnApplyWindowInsetsListener(root, (gamePageRoot, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            gamePageRoot.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            gamePageRoot.post(() ->
                    configureBatonBounds(batonView, space1, space2, referenceSquare)
            );

            return insets;
        });

        // 🎮 Layout change
        root.addOnLayoutChangeListener((v, left, top, right, bottom,
                                        oldLeft, oldTop, oldRight, oldBottom) ->
                configureBatonBounds(batonView, space1, space2, referenceSquare)
        );

        // 🎤 Micro
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

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && rotationVectorSensor != null) {
            sensorManager.registerListener(
                    orientationListener,
                    rotationVectorSensor,
                    SensorManager.SENSOR_DELAY_GAME
            );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(orientationListener);
        }

        if (audioRecord != null) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    private void configureBatonBounds(BatonView batonView, View space1, View space2, View referenceSquare) {
        float ySpace1 = getCenterYInViewCoordinates(space1, batonView);
        float ySpace2 = getCenterYInViewCoordinates(space2, batonView);

        float minY = Math.min(ySpace1, ySpace2);
        float maxY = Math.max(ySpace1, ySpace2);

        batonView.setMovementBounds(minY, maxY);
        batonView.setCupSizeFromSquare(referenceSquare.getWidth(), referenceSquare.getHeight());
        batonView.moveTo(ySpace1);
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