package com.alim.greennote.ui.activity;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alim.greennote.R;
import com.alim.greennote.databinding.ActivityAddNoteBinding;
import com.alim.greennote.databinding.ActivityMainBinding;
import com.nelu.ncbase.base.BaseActivity;

public class AddNoteActivity extends BaseActivity<ActivityAddNoteBinding> {

    @Override
    public void afterViewCreate(@NonNull ActivityAddNoteBinding activityAddNoteBinding) {
        EdgeToEdge.enable(this);
        initSystemPadding();
        back(activityAddNoteBinding.back);
    }

    private void initSystemPadding() {
        View statusBar = findViewById(R.id.status);
        View mainContent = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(statusBar, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(mainContent, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, 0, 0, systemBars.bottom);
                return insets;
            }
        });
    }
}