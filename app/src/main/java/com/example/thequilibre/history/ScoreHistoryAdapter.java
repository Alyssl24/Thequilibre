package com.example.thequilibre.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thequilibre.GamePageActivity;
import com.example.thequilibre.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScoreHistoryAdapter extends RecyclerView.Adapter<ScoreHistoryAdapter.HistoryViewHolder> {

    private final LayoutInflater inflater;
    private final DateFormat dateFormat;
    private final List<ScoreHistoryEntry> items = new ArrayList<>();

    public ScoreHistoryAdapter(@NonNull Context context) {
        inflater = LayoutInflater.from(context);
        dateFormat = new SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault());
    }

    public void submitList(@NonNull List<ScoreHistoryEntry> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_score_history, parent, false);
        return new HistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ScoreHistoryEntry entry = items.get(position);
        holder.scoreText.setText(
                holder.itemView.getContext().getString(R.string.history_score_label, entry.getScore())
        );
        holder.difficultyText.setText(entry.getDifficulty());
        holder.dateText.setText(formatTimestamp(entry.getTimestampMs(), holder.itemView.getContext()));

        if (GamePageActivity.DIFFICULTY_EASY.equals(entry.getDifficulty())) {
            holder.difficultyText.setBackgroundResource(R.drawable.bg_history_chip_easy);
            holder.difficultyText.setTextColor(0xFF275C44);
        } else if (GamePageActivity.DIFFICULTY_HARD.equals(entry.getDifficulty())) {
            holder.difficultyText.setBackgroundResource(R.drawable.bg_history_chip_hard);
            holder.difficultyText.setTextColor(0xFF7C2F2A);
        } else {
            holder.difficultyText.setBackgroundResource(R.drawable.bg_history_chip_medium);
            holder.difficultyText.setTextColor(0xFF5C3F8A);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    private String formatTimestamp(long timestampMs, @NonNull Context context) {
        if (timestampMs <= 0L) {
            return context.getString(R.string.history_unknown_date);
        }
        return dateFormat.format(new Date(timestampMs));
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        final TextView scoreText;
        final TextView difficultyText;
        final TextView dateText;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            scoreText = itemView.findViewById(R.id.history_item_score);
            difficultyText = itemView.findViewById(R.id.history_item_difficulty);
            dateText = itemView.findViewById(R.id.history_item_date);
        }
    }
}
