package me.srikavin.quiz.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import me.srikavin.quiz.R;

public class QuizEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_edit_activity);
        Intent intent = getIntent();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container,
                            QuizEditFragment.Companion.newInstance((QuizEditFragment.Mode) intent.getSerializableExtra("mode"),
                                    intent.getStringExtra("id")))
                    .commitNow();
        }
    }
}
