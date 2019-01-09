package me.srikavin.quiz.view.game;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Return to Home");
        builder.setMessage("Are you sure you want to return to the main screen?");
        builder.setPositiveButton("Exit", (dialog, id) -> {
            super.onBackPressed();
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();

    }

    void goToStatsDisplay(GameRepository.GameStats stats) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, GameStatsFragment.newInstance(stats))
                .commitNow();
    }
}
