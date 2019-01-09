package me.srikavin.quiz.view.game;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.jinatonic.confetti.CommonConfetti;
import com.google.gson.Gson;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java9.util.stream.Collectors;
import java9.util.stream.StreamSupport;
import me.srikavin.quiz.R;
import me.srikavin.quiz.model.QuizAnswer;
import me.srikavin.quiz.model.QuizQuestion;
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

        correctFraction.setText(getString(R.string.game_stats_correct_fraction_text, stats.correct, stats.total));
        correctPercentage.setText(getString(R.string.game_stats_correct_percentage_text, stats.percentCorrect * 100));

        QuestionAdapter adapter = new QuestionAdapter(stats);

        RecyclerView recyclerView = getView().findViewById(R.id.game_stats_questions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        if (stats.percentCorrect > 0.85) {
            new Handler().postDelayed(() -> {
                CommonConfetti.rainingConfetti(getView().findViewById(R.id.game_stats_confetti_container), new int[]{
                        Color.GREEN, Color.CYAN,
                        getResources().getColor(R.color.colorPrimary, null),
                        getResources().getColor(R.color.colorSecondary, null)
                }).getConfettiManager()
                        .setEmissionDuration(50 + stats.score)
                        .setVelocityY(600, 75)
                        .setVelocityX(0, 200)
                        .setEmissionRate(40 + stats.score * .25f).animate();
            }, 1200);
        }
    }

    private class QuestionAdapter extends RecyclerView.Adapter<QuestionCard> {
        private List<QuizQuestion> questions;
        private List<QuizAnswer> chosen;

        public QuestionAdapter(GameRepository.GameStats stats) {
            questions = stats.quizQuestions;
            chosen = stats.chosen;
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

        void bind(QuizQuestion question, QuizAnswer chosen) {
            RecyclerView recyclerView = itemView.findViewById(R.id.game_question_answer_list);
            TextView title = itemView.findViewById(R.id.game_question_title);

            GameAnswerAdapter answerAdapter = new GameAnswerAdapter(recyclerView, (qa) -> {
            }, getContext());

            title.setText(question.text);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(answerAdapter);

            answerAdapter.setAnswers(question.answers);

            List<QuizAnswer> correct = StreamSupport.stream(question.answers)
                    .filter((e) -> e.correct).collect(Collectors.toList());

            answerAdapter.setOnBindAnswer((viewHolder, answer) -> {
                if (chosen.equals(answer)) {
                    if (chosen.correct) {
                        viewHolder.displayAsChosenCorrect();
                    } else {
                        viewHolder.displayAsChosenIncorrect();
                    }
                } else {
                    if (correct.contains(answer)) {
                        viewHolder.displayAsCorrect();
                    }
                }

            });
        }
    }
}
