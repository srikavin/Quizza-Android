package me.srikavin.quiz.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import me.srikavin.quiz.R;

public class QuizDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_detail_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, QuizDetailFragment.newInstance(getIntent().getStringExtra("id")))
                    .commitNow();
        }
    }
}
