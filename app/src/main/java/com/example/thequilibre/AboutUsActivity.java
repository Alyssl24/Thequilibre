package com.example.thequilibre;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about_us);

        View root = findViewById(R.id.about_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.about_back_link).setOnClickListener(view -> finish());

        bindExternalLink(R.id.corentin_linkedin, "https://www.linkedin.com/in/corentin-jere/");
        bindExternalLink(R.id.corentin_github, "https://github.com/CJs0800/");

        bindExternalLink(R.id.alex_linkedin, "https://www.linkedin.com/in/alex-lecomte-3a6503296/");
        bindExternalLink(R.id.alex_github, "https://github.com/alexLcmt");

        bindExternalLink(R.id.alyssia_linkedin, "https://www.linkedin.com/in/alyssia-leclerc-ab1839313/");
        bindExternalLink(R.id.alyssia_github, "https://github.com/Alyssl24/");
    }

    private void bindExternalLink(int viewId, String url) {
        findViewById(viewId).setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }
}
