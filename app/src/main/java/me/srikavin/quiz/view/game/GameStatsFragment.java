package me.srikavin.quiz.view.game;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.jinatonic.confetti.CommonConfetti;
import com.google.gson.Gson;

import java.util.List;

import java9.util.stream.Collectors;
import java9.util.stream.StreamSupport;
import kotlin.Unit;
import me.srikavin.quiz.R;
import me.srikavin.quiz.model.QuizQuestion;
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        assert getArguments() != null;

        stats = gson.fromJson(getArguments().getString(ARG_STATS_JSON), GameRepository.GameStats.class);

        TextView correctFraction = getView().findViewById(R.id.game_stats_correct_fraction);
        TextView correctPercentage = getView().findViewById(R.id.game_stats_correct_percentage);

        correctFraction.setText(getString(R.string.game_stats_correct_fraction_text, stats.getCorrect(), stats.getTotal()));
        correctPercentage.setText(getString(R.string.game_stats_correct_percentage_text, stats.getPercentCorrect() * 100));

        QuestionAdapter adapter = new QuestionAdapter(stats);

        RecyclerView recyclerView = getView().findViewById(R.id.game_stats_questions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        if (stats.getPercentCorrect() > 0.85) {
            new Handler().postDelayed(() -> {
                CommonConfetti.rainingConfetti(getView().findViewById(R.id.game_stats_confetti_container), new int[]{
                        Color.GREEN, Color.CYAN,
                        ContextCompat.getColor(getContext(), R.color.colorPrimary),
                        ContextCompat.getColor(getContext(), R.color.colorSecondary),
                }).getConfettiManager()
                        .setEmissionDuration(50 + stats.getScore())
                        .setVelocityY(600, 75)
                        .setVelocityX(0, 200)
                        .setEmissionRate(40 + stats.getScore() * .25f).animate();
            }, 1200);
        }
    }

    private class QuestionAdapter extends RecyclerView.Adapter<QuestionCard> {
        private List<QuizQuestion> questions;
        private List<QuizAnswerModel> chosen;

        public QuestionAdapter(GameRepository.GameStats stats) {
            questions = stats.getQuizQuestions();
            chosen = stats.getChosen();
        }

        @NonNull
        @Override
        public QuestionCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new QuestionCard(LayoutInflater.from(getContext()).inflate(R.layout.game_question_card, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull QuestionCard holder, int position) {
            holder.bind(questions.get(position), chosen.get(position));
        }

        @Override
        public int getItemCount() {
            return questions.size();
        }
    }

    private class QuestionCard extends RecyclerView.ViewHolder {
        QuestionCard(@NonNull View itemView) {
            super(itemView);
        }

        void bind(QuizQuestion question, QuizAnswerModel chosen) {
            RecyclerView recyclerView = itemView.findViewById(R.id.game_question_answer_list);
            TextView title = itemView.findViewById(R.id.game_question_title);

            GameAnswerAdapter answerAdapter = new GameAnswerAdapter(recyclerView, (qa) -> Unit.INSTANCE, getContext());

            title.setText(question.getContents());
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(answerAdapter);

            answerAdapter.setAnswers(question.getAnswers());

            List<QuizAnswerModel> correct = StreamSupport.stream(question.getAnswers())
                    .filter(QuizAnswerModel::isCorrect).collect(Collectors.toList());

            answerAdapter.setOnBindAnswer((viewHolder, answer) -> {
                if (chosen.equals(answer)) {
                    if (chosen.isCorrect()) {
                        viewHolder.displayAsChosenCorrect();
                    } else {
                        viewHolder.displayAsChosenIncorrect();
                    }
                } else {
                    if (correct.contains(answer)) {
                        viewHolder.displayAsCorrect();
                    }
                }
                return Unit.INSTANCE;
            });
        }
    }
}
