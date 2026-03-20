package com.example.thequilibre;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.thequilibre.model.GameView;

public class GamePageActivity extends AppCompatActivity {

    public static final String DIFFICULTY_MEDIUM = "Medium";
    public static final String DIFFICULTY_EASY = "Easy";
    public static final String DIFFICULTY_HARD = "Hard";
    public static final String EXTRA_DIFFICULTY = "";
    private static final float ROTATION_SMOOTHING = 0.18f;
    private static final float MAX_BATON_ROTATION_DEGREES = 50f;

    private BatonView batonView;
    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private final float[] rotationMatrix = new float[9];
    private final float[] remappedRotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private float filteredRotationDegrees;

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
    }

    private void configureBatonBounds(BatonView batonView, View space1, View space2) {
        float ySpace1 = space1.getY() + (space1.getHeight() / 2f);
        float ySpace2 = space2.getY() + (space2.getHeight() / 2f);

        float minY = Math.min(ySpace1, ySpace2);
        float maxY = Math.max(ySpace1, ySpace2);

        batonView.setMovementBounds(minY, maxY);
        batonView.moveTo(ySpace1);
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
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // No-op
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_page);

        FrameLayout container = findViewById(R.id.game_container);
        GameView gameView = new GameView(this);
        container.addView(gameView);

        View root = findViewById(R.id.main);
        batonView = findViewById(R.id.baton_view);
        View space1 = findViewById(R.id.space_1);
        View space2 = findViewById(R.id.space_2);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }
        ViewCompat.setOnApplyWindowInsetsListener(root, (gamePageRoot, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            gamePageRoot.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        root.addOnLayoutChangeListener((v, left, top, right, bottom,
                                        oldLeft, oldTop, oldRight, oldBottom) ->
                configureBatonBounds(batonView, space1, space2)
        );
    }

}
