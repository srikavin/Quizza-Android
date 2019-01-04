package me.srikavin.quiz.view.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import me.srikavin.quiz.R;
import me.srikavin.quiz.repository.GameRepository;

public class GameStatsFragment extends Fragment {
    private static final String ARG_STATS_JSON = "stats_json";
    private static final Gson gson = new Gson();

    private GameRepository.GameStats stats;

    public GameStatsFragment() {
        // Required empty public constructor
    }

    public static GameStatsFragment newInstance(GameRepository.GameStats stats) {
        GameStatsFragment fragment = new GameStatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATS_JSON, gson.toJson(stats));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.game_finished_stats, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null;

        stats = gson.fromJson(getArguments().getString(ARG_STATS_JSON), GameRepository.GameStats.class);

    }
}
