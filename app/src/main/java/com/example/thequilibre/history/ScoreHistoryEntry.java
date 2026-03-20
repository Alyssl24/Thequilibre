package com.example.thequilibre.history;

import androidx.annotation.NonNull;

public class ScoreHistoryEntry {

    private final int score;
    @NonNull
    private final String difficulty;
    private final long timestampMs;

    public ScoreHistoryEntry(int score, @NonNull String difficulty, long timestampMs) {
        this.score = Math.max(0, score);
        this.difficulty = difficulty;
        this.timestampMs = Math.max(0L, timestampMs);
    }

    public int getScore() {
        return score;
    }

    @NonNull
    public String getDifficulty() {
        return difficulty;
    }

    public long getTimestampMs() {
        return timestampMs;
    }
}
