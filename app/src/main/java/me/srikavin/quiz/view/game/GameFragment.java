package me.srikavin.quiz.view.game;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jinatonic.confetti.CommonConfetti;

import java.text.MessageFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.srikavin.quiz.MainActivity;
import me.srikavin.quiz.R;
import me.srikavin.quiz.model.QuizGameState;
import me.srikavin.quiz.repository.GameRepository;
import me.srikavin.quiz.viewmodel.GameViewModel;

public class GameFragment extends Fragment {

    private GameViewModel mViewModel;

    public static GameFragment newInstance(String id) {
        GameFragment fragment = new GameFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.game_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(GameViewModel.class);

        TextView title = getView().findViewById(R.id.game_question_title);
        RecyclerView answerRecycler = getView().findViewById(R.id.game_question_answer_list);
        ProgressBar countdownBar = getView().findViewById(R.id.game_time_progress_bar);
        TextView countdownText = getView().findViewById(R.id.game_time_countdown);
        TextView gamePosition = getView().findViewById(R.id.game_position);
        ViewGroup container = getView().findViewById(R.id.game_container);

        GameAnswerAdapter adapter = new GameAnswerAdapter(answerRecycler, mViewModel::submitAnswer, getContext());

        answerRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        answerRecycler.setAdapter(adapter);

        assert getArguments() != null;
        assert getArguments().getString("id") != null;

        String id = getArguments().getString("id");

        AtomicInteger numberOfQuestions = new AtomicInteger();
        AtomicInteger currentQuestion = new AtomicInteger();

        mViewModel.getQuizByID(id).observe(this, quiz -> {
            if (quiz == null) {
                Toast.makeText(getActivity(), "Failed to load quiz", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return;
            }
            mViewModel.createGame(quiz);
        });

        mViewModel.getGameInfo().observe(this, info -> {
            countdownBar.setMax(info.getTimePerQuestion());
            numberOfQuestions.set(info.getNumberOfQuestions());
            gamePosition.setText(getString(R.string.game_position, currentQuestion.get(), numberOfQuestions.get()));
        });

        mViewModel.getCurrentQuestion().observe(this, question -> {
            adapter.setAnswers(question.answers);
            title.setText(question.text);
        });
        mViewModel.getTimeRemaining().observe(this, timeLeft -> {
            countdownText.setText(MessageFormat.format("{0}", timeLeft));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                countdownBar.setProgress(timeLeft, true);
            } else {
                countdownBar.setProgress(timeLeft);
            }
        });

        mViewModel.getGameState().observe(this, state -> {
            currentQuestion.set(state.getCurrentQuestion() + 1);
            gamePosition.setText(getString(R.string.game_position, currentQuestion.get(), numberOfQuestions.get()));
        });

        mViewModel.getAnswerResponse().observe(this, response -> {
            adapter.displayCorrectAnswers(response.getCorrectAnswers());
            if (response.isCorrect()) {
                // The chosen answer was correct
                CommonConfetti.rainingConfetti(container, new int[]{
                        Color.GREEN, Color.CYAN,
                        getResources().getColor(R.color.colorPrimary, null),
                        getResources().getColor(R.color.colorSecondary, null)
                }).getConfettiManager()
                        .setEmissionDuration(750 + countdownBar.getProgress() * 10)
                        .setVelocityY(600, 75)
                        .setVelocityX(0, 200)
                        .setEmissionRate(countdownBar.getProgress() * 2f).animate();
                adapter.displayLastAsCorrect();
            } else {
                // The chosen answer was wrong
                if (countdownBar.getProgress() > 0) {
                    adapter.displayLastAsIncorrect();
                }
            }
        });

        mViewModel.getGameStats().observe(this, (stats -> {
            if (mViewModel.getGameState().getValue() == QuizGameState.FINISHED) {
                displayStatsFragment(stats);
            }
        }));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(() -> {
                    if (mViewModel.getGameState().getValue() == QuizGameState.FINISHED) {
                        displayStatsFragment(mViewModel.getGameStats().getValue());
                    }
                });
            }
        }, 1000);
    }

    private void displayStatsFragment(GameRepository.GameStats stats) {
        ((GameActivity) getActivity()).goToStatsDisplay(stats);
    }

}
