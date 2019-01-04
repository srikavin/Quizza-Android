package me.srikavin.quiz.view.game;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import me.srikavin.quiz.R;
import me.srikavin.quiz.repository.GameRepository;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, GameFragment.newInstance(getIntent().getStringExtra("id")))
                    .commitNow();
        }
    }

    void goToStatsDisplay(GameRepository.GameStats stats) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, GameStatsFragment.newInstance(stats))
                .commitNow();
    }
}
