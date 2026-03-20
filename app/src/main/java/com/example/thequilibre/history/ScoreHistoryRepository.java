package com.example.thequilibre.history;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.thequilibre.GamePageActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScoreHistoryRepository {

    private static final String PREFS_NAME = "score_history_prefs";
    private static final String KEY_HISTORY_JSON = "history_json";
    private static final int MAX_ENTRIES = 500;

    private final SharedPreferences sharedPreferences;
    private final Object lock = new Object();

    public ScoreHistoryRepository(@NonNull Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveScore(int score, @Nullable String difficulty) {
        synchronized (lock) {
            JSONArray currentArray = readHistoryArraySafely();
            JSONArray updatedArray = new JSONArray();

            JSONObject newEntry = new JSONObject();
            try {
                newEntry.put("score", Math.max(0, score));
                newEntry.put("difficulty", normalizeDifficulty(difficulty));
                newEntry.put("timestamp", System.currentTimeMillis());
                updatedArray.put(newEntry);
            } catch (Exception ignored) {
                // If the new item cannot be serialized, keep the existing history unchanged.
                return;
            }

            int remainingSlots = MAX_ENTRIES - 1;
            for (int i = 0; i < currentArray.length() && remainingSlots > 0; i++) {
                Object item = currentArray.opt(i);
                if (item instanceof JSONObject) {
                    updatedArray.put(item);
                    remainingSlots--;
                }
            }

            sharedPreferences.edit().putString(KEY_HISTORY_JSON, updatedArray.toString()).apply();
        }
    }

    @NonNull
    public List<ScoreHistoryEntry> getHistory() {
        synchronized (lock) {
            JSONArray historyArray = readHistoryArraySafely();
            List<ScoreHistoryEntry> history = new ArrayList<>(historyArray.length());

            for (int i = 0; i < historyArray.length(); i++) {
                JSONObject jsonObject = historyArray.optJSONObject(i);
                if (jsonObject == null) {
                    continue;
                }
                int score = jsonObject.optInt("score", 0);
                String difficulty = normalizeDifficulty(jsonObject.optString("difficulty", null));
                long timestamp = jsonObject.optLong("timestamp", 0L);
                history.add(new ScoreHistoryEntry(score, difficulty, timestamp));
            }
            return history;
        }
    }

    private JSONArray readHistoryArraySafely() {
        String raw = sharedPreferences.getString(KEY_HISTORY_JSON, null);
        if (raw == null || raw.trim().isEmpty()) {
            return new JSONArray();
        }
        try {
            return new JSONArray(raw);
        } catch (Exception ignored) {
            // Corrupted data: reset to a safe empty state.
            sharedPreferences.edit().remove(KEY_HISTORY_JSON).apply();
            return new JSONArray();
        }
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
