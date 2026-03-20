package com.example.thequilibre;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thequilibre.history.ScoreHistoryAdapter;
import com.example.thequilibre.history.ScoreHistoryEntry;
import com.example.thequilibre.history.ScoreHistoryRepository;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_POINT = "com.example.thequilibre.extra.HISTORY_ENTRY_POINT";
    public static final String ENTRY_POINT_HOME = "home";
    public static final String ENTRY_POINT_END_SCREEN = "end_screen";

    private ScoreHistoryRepository scoreHistoryRepository;
    private ScoreHistoryAdapter scoreHistoryAdapter;
    private RecyclerView historyRecyclerView;
    private View emptyStateView;

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull String entryPoint) {
        Intent intent = new Intent(context, HistoryActivity.class);
        intent.putExtra(EXTRA_ENTRY_POINT, entryPoint);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        View root = findViewById(R.id.history_root);
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

        scoreHistoryRepository = new ScoreHistoryRepository(this);
        scoreHistoryAdapter = new ScoreHistoryAdapter(this);

        historyRecyclerView = findViewById(R.id.history_recycler);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(scoreHistoryAdapter);
        historyRecyclerView.setHasFixedSize(true);

        emptyStateView = findViewById(R.id.history_empty_state);

        findViewById(R.id.history_back_link).setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderHistory();
    }

    private void renderHistory() {
        List<ScoreHistoryEntry> entries = scoreHistoryRepository.getHistory();
        scoreHistoryAdapter.submitList(entries);

        boolean isEmpty = entries.isEmpty();
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        historyRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
