package com.example.thequilibre.game;

import android.content.Context;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.thequilibre.BatonView;
import com.example.thequilibre.GamePageActivity;
import com.example.thequilibre.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ObstacleSystem {

    public interface Listener {
        void onDangerousCollision(int slotIndex);
    }

    private enum ObstacleState {
        GROWING,
        DANGEROUS
    }

    private static final int SLOT_COUNT = 6;
    private static final int HARD_ACTIVE_LIMIT = SLOT_COUNT - 1;
    private static final float MIN_SCALE = 0.15f;
    private static final long FRAME_DELAY_MS = 16L;
    private static final long SPAWN_RETRY_DELAY_MS = 160L;

    private final Context context;
    private final FrameLayout overlay;
    private final List<View> slotViews;
    private final BatonView batonView;
    private final Listener listener;
    private final DifficultyProfile difficultyProfile;
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SparseArray<ObstacleInstance> activeBySlot = new SparseArray<>();
    private final RectF[] slotRects = new RectF[SLOT_COUNT];
    private final RectF tempScreenRect = new RectF();
    private final int[] overlayLocationOnScreen = new int[2];
    private final int[] slotLocationOnScreen = new int[2];
    private final Runnable frameRunnable = this::tick;

    private boolean running;
    private boolean paused;
    private long runStartedAtMs;
    private long pauseStartedAtMs;
    private long nextSpawnAtMs;
    private int lastSpawnSlot = -1;

    public ObstacleSystem(@NonNull Context context,
                          @NonNull FrameLayout overlay,
                          @NonNull List<View> slotViews,
                          @NonNull BatonView batonView,
                          @Nullable String difficultyLabel,
                          @NonNull Listener listener) {
        if (slotViews.size() != SLOT_COUNT) {
            throw new IllegalArgumentException("ObstacleSystem expects exactly 6 slots.");
        }
        this.context = context;
        this.overlay = overlay;
        this.slotViews = slotViews;
        this.batonView = batonView;
        this.listener = listener;
        this.difficultyProfile = DifficultyProfile.fromLabel(difficultyLabel);

        for (int i = 0; i < SLOT_COUNT; i++) {
            slotRects[i] = new RectF();
            this.slotViews.get(i).setAlpha(0.58f);
        }
    }

    public void resumeOrStart() {
        if (!running) {
            startNewRun();
            return;
        }
        if (!paused) {
            return;
        }
        long now = SystemClock.uptimeMillis();
        long pausedDuration = now - pauseStartedAtMs;
        runStartedAtMs += pausedDuration;
        nextSpawnAtMs += pausedDuration;

        for (int i = 0; i < activeBySlot.size(); i++) {
            ObstacleInstance obstacle = activeBySlot.valueAt(i);
            obstacle.spawnedAtMs += pausedDuration;
            if (obstacle.dangerousSinceMs > 0L) {
                obstacle.dangerousSinceMs += pausedDuration;
            }
        }
        paused = false;
        scheduleNextFrame();
    }

    public void pause() {
        if (!running || paused) {
            return;
        }
        paused = true;
        pauseStartedAtMs = SystemClock.uptimeMillis();
        handler.removeCallbacks(frameRunnable);
    }

    public void stop() {
        running = false;
        paused = false;
        handler.removeCallbacks(frameRunnable);
        clearAllObstacles();
    }

    public void updateLayoutBounds() {
        if (overlay.getWidth() <= 0 || overlay.getHeight() <= 0) {
            return;
        }

        overlay.getLocationOnScreen(overlayLocationOnScreen);
        for (int i = 0; i < SLOT_COUNT; i++) {
            View slotView = slotViews.get(i);
            if (slotView.getWidth() <= 0 || slotView.getHeight() <= 0) {
                continue;
            }
            slotView.getLocationOnScreen(slotLocationOnScreen);
            float left = slotLocationOnScreen[0] - overlayLocationOnScreen[0];
            float top = slotLocationOnScreen[1] - overlayLocationOnScreen[1];
            slotRects[i].set(
                    left,
                    top,
                    left + slotView.getWidth(),
                    top + slotView.getHeight()
            );
        }
        for (int i = 0; i < activeBySlot.size(); i++) {
            ObstacleInstance obstacle = activeBySlot.valueAt(i);
            applyObstacleLayout(obstacle);
        }
    }

    private void startNewRun() {
        clearAllObstacles();
        running = true;
        paused = false;
        lastSpawnSlot = -1;
        runStartedAtMs = SystemClock.uptimeMillis();
        nextSpawnAtMs = runStartedAtMs + difficultyProfile.spawnDelayMs(0L);
        scheduleNextFrame();
    }

    private void tick() {
        if (!running || paused) {
            return;
        }
        if (!hasValidLayout()) {
            scheduleNextFrame();
            return;
        }

        long now = SystemClock.uptimeMillis();
        long elapsedMs = now - runStartedAtMs;

        if (now >= nextSpawnAtMs) {
            boolean spawned = trySpawnObstacle(now, elapsedMs);
            nextSpawnAtMs = now + (spawned
                    ? randomizedSpawnDelay(elapsedMs)
                    : SPAWN_RETRY_DELAY_MS);
        }

        updateObstacles(now);
        scheduleNextFrame();
    }

    private void scheduleNextFrame() {
        handler.removeCallbacks(frameRunnable);
        handler.postDelayed(frameRunnable, FRAME_DELAY_MS);
    }

    private boolean hasValidLayout() {
        for (RectF slotRect : slotRects) {
            if (slotRect.width() <= 0f || slotRect.height() <= 0f) {
                return false;
            }
        }
        return true;
    }

    private long randomizedSpawnDelay(long elapsedMs) {
        long baseDelay = difficultyProfile.spawnDelayMs(elapsedMs);
        float jitter = 0.85f + (random.nextFloat() * 0.30f);
        return Math.max(320L, (long) (baseDelay * jitter));
    }

    private boolean trySpawnObstacle(long nowMs, long elapsedMs) {
        int maxSimultaneous = Math.min(
                HARD_ACTIVE_LIMIT,
                difficultyProfile.maxSimultaneous(elapsedMs)
        );
        if (activeBySlot.size() >= maxSimultaneous) {
            return false;
        }
        if (activeBySlot.size() >= HARD_ACTIVE_LIMIT) {
            return false;
        }

        List<Integer> candidates = new ArrayList<>();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (activeBySlot.get(slot) == null) {
                candidates.add(slot);
            }
        }
        if (candidates.isEmpty()) {
            return false;
        }

        if (candidates.size() > 1) {
            candidates.remove(Integer.valueOf(lastSpawnSlot));
        }
        int chosenSlot = candidates.get(random.nextInt(candidates.size()));
        spawnAtSlot(chosenSlot, nowMs, elapsedMs);
        lastSpawnSlot = chosenSlot;
        return true;
    }

    private void spawnAtSlot(int slotIndex, long nowMs, long elapsedMs) {
        RectF slotRect = slotRects[slotIndex];
        View obstacleView = new View(context);
        obstacleView.setBackgroundResource(R.drawable.obstacle_growing_tile);
        obstacleView.setScaleX(MIN_SCALE);
        obstacleView.setScaleY(MIN_SCALE);
        obstacleView.setAlpha(0.96f);
        obstacleView.setClickable(false);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                Math.round(slotRect.width()),
                Math.round(slotRect.height())
        );
        params.leftMargin = Math.round(slotRect.left);
        params.topMargin = Math.round(slotRect.top);
        overlay.addView(obstacleView, params);

        ObstacleInstance obstacle = new ObstacleInstance(
                slotIndex,
                obstacleView,
                nowMs,
                difficultyProfile.growthDurationMs(elapsedMs),
                difficultyProfile.dangerDurationMs(elapsedMs)
        );
        activeBySlot.put(slotIndex, obstacle);
        applyObstacleLayout(obstacle);
    }

    private void applyObstacleLayout(@NonNull ObstacleInstance obstacle) {
        RectF slotRect = slotRects[obstacle.slotIndex];
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) obstacle.view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new FrameLayout.LayoutParams(
                    Math.round(slotRect.width()),
                    Math.round(slotRect.height())
            );
        } else {
            layoutParams.width = Math.round(slotRect.width());
            layoutParams.height = Math.round(slotRect.height());
        }
        layoutParams.leftMargin = Math.round(slotRect.left);
        layoutParams.topMargin = Math.round(slotRect.top);
        obstacle.view.setLayoutParams(layoutParams);
        obstacle.view.setPivotX(slotRect.width() / 2f);
        obstacle.view.setPivotY(slotRect.height() / 2f);
    }

    private void updateObstacles(long nowMs) {
        overlay.getLocationOnScreen(overlayLocationOnScreen);
        for (int i = activeBySlot.size() - 1; i >= 0; i--) {
            int slotIndex = activeBySlot.keyAt(i);
            ObstacleInstance obstacle = activeBySlot.valueAt(i);
            if (obstacle == null) {
                continue;
            }

            if (obstacle.state == ObstacleState.GROWING) {
                float growthProgress = (nowMs - obstacle.spawnedAtMs) / (float) obstacle.growthDurationMs;
                float clamped = clamp01(growthProgress);
                float eased = easeOutCubic(clamped);
                float scale = MIN_SCALE + (1f - MIN_SCALE) * eased;
                obstacle.view.setScaleX(scale);
                obstacle.view.setScaleY(scale);
                if (clamped >= 1f) {
                    obstacle.state = ObstacleState.DANGEROUS;
                    obstacle.dangerousSinceMs = nowMs;
                    obstacle.view.setScaleX(1f);
                    obstacle.view.setScaleY(1f);
                    obstacle.view.setBackgroundResource(R.drawable.obstacle_dangerous_tile);
                }
                continue;
            }

            if (obstacle.state == ObstacleState.DANGEROUS) {
                toScreenRect(slotRects[slotIndex], tempScreenRect);
                if (batonView.intersectsRectOnScreen(tempScreenRect)) {
                    listener.onDangerousCollision(slotIndex);
                    removeObstacle(i);
                    continue;
                }
                if ((nowMs - obstacle.dangerousSinceMs) >= obstacle.dangerDurationMs) {
                    removeObstacle(i);
                }
            }
        }
    }

    private void toScreenRect(@NonNull RectF overlayRect, @NonNull RectF outScreenRect) {
        outScreenRect.set(
                overlayRect.left + overlayLocationOnScreen[0],
                overlayRect.top + overlayLocationOnScreen[1],
                overlayRect.right + overlayLocationOnScreen[0],
                overlayRect.bottom + overlayLocationOnScreen[1]
        );
    }

    private void removeObstacle(int indexInSparseArray) {
        ObstacleInstance obstacle = activeBySlot.valueAt(indexInSparseArray);
        if (obstacle != null) {
            overlay.removeView(obstacle.view);
        }
        activeBySlot.removeAt(indexInSparseArray);
    }

    private void clearAllObstacles() {
        for (int i = activeBySlot.size() - 1; i >= 0; i--) {
            ObstacleInstance obstacle = activeBySlot.valueAt(i);
            if (obstacle != null) {
                overlay.removeView(obstacle.view);
            }
        }
        activeBySlot.clear();
    }

    private float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private float easeOutCubic(float value) {
        float inverse = 1f - value;
        return 1f - (inverse * inverse * inverse);
    }

    private static class ObstacleInstance {
        final int slotIndex;
        final View view;
        final long growthDurationMs;
        final long dangerDurationMs;
        long spawnedAtMs;
        long dangerousSinceMs;
        ObstacleState state = ObstacleState.GROWING;

        ObstacleInstance(int slotIndex,
                         @NonNull View view,
                         long spawnedAtMs,
                         long growthDurationMs,
                         long dangerDurationMs) {
            this.slotIndex = slotIndex;
            this.view = view;
            this.spawnedAtMs = spawnedAtMs;
            this.growthDurationMs = growthDurationMs;
            this.dangerDurationMs = dangerDurationMs;
        }
    }

    private static class DifficultyProfile {
        final long baseSpawnDelayMs;
        final long minSpawnDelayMs;
        final float spawnDecayPerSecond;
        final long baseGrowthDurationMs;
        final long minGrowthDurationMs;
        final float growthDecayPerSecond;
        final long baseDangerDurationMs;
        final long minDangerDurationMs;
        final float dangerDecayPerSecond;
        final int initialMaxSimultaneous;
        final long[] maxSimThresholdsMs;

        DifficultyProfile(long baseSpawnDelayMs,
                          long minSpawnDelayMs,
                          float spawnDecayPerSecond,
                          long baseGrowthDurationMs,
                          long minGrowthDurationMs,
                          float growthDecayPerSecond,
                          long baseDangerDurationMs,
                          long minDangerDurationMs,
                          float dangerDecayPerSecond,
                          int initialMaxSimultaneous,
                          long[] maxSimThresholdsMs) {
            this.baseSpawnDelayMs = baseSpawnDelayMs;
            this.minSpawnDelayMs = minSpawnDelayMs;
            this.spawnDecayPerSecond = spawnDecayPerSecond;
            this.baseGrowthDurationMs = baseGrowthDurationMs;
            this.minGrowthDurationMs = minGrowthDurationMs;
            this.growthDecayPerSecond = growthDecayPerSecond;
            this.baseDangerDurationMs = baseDangerDurationMs;
            this.minDangerDurationMs = minDangerDurationMs;
            this.dangerDecayPerSecond = dangerDecayPerSecond;
            this.initialMaxSimultaneous = initialMaxSimultaneous;
            this.maxSimThresholdsMs = maxSimThresholdsMs;
        }

        static DifficultyProfile fromLabel(@Nullable String difficultyLabel) {
            if (GamePageActivity.DIFFICULTY_EASY.equals(difficultyLabel)) {
                return new DifficultyProfile(
                        2300L, 1050L, 4.2f,
                        2200L, 1400L, 2.0f,
                        950L, 700L, 1.0f,
                        2,
                        new long[]{45_000L, 120_000L, 240_000L}
                );
            }
            if (GamePageActivity.DIFFICULTY_HARD.equals(difficultyLabel)) {
                return new DifficultyProfile(
                        1450L, 560L, 5.5f,
                        1500L, 750L, 3.2f,
                        800L, 520L, 1.1f,
                        4,
                        new long[]{25_000L}
                );
            }
            return new DifficultyProfile(
                    1800L, 780L, 4.8f,
                    1800L, 1000L, 2.7f,
                    900L, 620L, 1.0f,
                    3,
                    new long[]{35_000L, 95_000L}
            );
        }

        long spawnDelayMs(long elapsedMs) {
            return decay(baseSpawnDelayMs, minSpawnDelayMs, spawnDecayPerSecond, elapsedMs);
        }

        long growthDurationMs(long elapsedMs) {
            return decay(baseGrowthDurationMs, minGrowthDurationMs, growthDecayPerSecond, elapsedMs);
        }

        long dangerDurationMs(long elapsedMs) {
            return decay(baseDangerDurationMs, minDangerDurationMs, dangerDecayPerSecond, elapsedMs);
        }

        int maxSimultaneous(long elapsedMs) {
            int maxSimultaneous = initialMaxSimultaneous;
            for (long threshold : maxSimThresholdsMs) {
                if (elapsedMs >= threshold) {
                    maxSimultaneous++;
                }
            }
            return Math.min(HARD_ACTIVE_LIMIT, maxSimultaneous);
        }

        private long decay(long base, long min, float decayPerSecond, long elapsedMs) {
            float elapsedSeconds = elapsedMs / 1000f;
            long value = Math.round(base - (elapsedSeconds * decayPerSecond));
            return Math.max(min, value);
        }
    }
}
