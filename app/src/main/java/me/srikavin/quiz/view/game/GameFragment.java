package me.srikavin.quiz.view.game;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java9.util.function.BiConsumer;
import java9.util.stream.StreamSupport;
import me.srikavin.quiz.MainActivity;
import me.srikavin.quiz.R;
import me.srikavin.quiz.model.QuizAnswer;
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

        GameAnswerAdapter adapter = new GameAnswerAdapter(answerRecycler);

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
            countdownBar.setMax(info.timePerQuestion);
            numberOfQuestions.set(info.numberOfQuestions);
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
                        .setEmissionDuration(500 + countdownBar.getProgress() * 100)
                        .setVelocityY(600, 75)
                        .setVelocityX(0, 200)
                        .setEmissionRate(countdownBar.getProgress() * 8f).animate();
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

        if (mViewModel.getGameState().getValue() == QuizGameState.FINISHED) {
            displayStatsFragment(mViewModel.getGameStats().getValue());
        }
    }

    private void displayStatsFragment(GameRepository.GameStats stats) {
        ((GameActivity) getActivity()).goToStatsDisplay(stats);
    }

    private class GameAnswerAdapter extends RecyclerView.Adapter<GameAnswerAdapter.AnswerViewHolder> {

        private List<QuizAnswer> answers = Collections.emptyList();
        private AnswerViewHolder lastChosen;
        private RecyclerView recycler;

        public GameAnswerAdapter(RecyclerView recycler) {
            this.recycler = recycler;
        }

        @NonNull
        @Override
        public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AnswerViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.game_answer_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
            holder.bind(answers.get(position), (vh, qa) -> {
                mViewModel.submitAnswer(qa);
                lastChosen = vh;
            });
        }

        @Override
        public int getItemCount() {
            return answers.size();
        }

        void setAnswers(List<QuizAnswer> answers) {
            this.answers = answers;
            notifyDataSetChanged();
        }

        void displayLastAsCorrect() {
            if (lastChosen != null) {
                lastChosen.displayAsCorrect();
            }
        }

        void displayLastAsIncorrect() {
            if (lastChosen != null) {
                lastChosen.displayAsIncorrect();
            }
        }

        void displayCorrectAnswers(List<QuizAnswer> correct) {
            StreamSupport.stream(answers).filter(correct::contains).forEach(e -> {
                AnswerViewHolder holder = ((AnswerViewHolder) recycler.findViewHolderForAdapterPosition(answers.indexOf(e)));
                if (holder != null) {
                    holder.displayAsCorrect();
                }
            });
        }

        class AnswerViewHolder extends RecyclerView.ViewHolder {
            private int correct_text = getResources().getColor(R.color.game_answer_correct_text, null);
            private int correct_bg = getResources().getColor(R.color.game_answer_correct_background, null);
            private int incorrect_text = getResources().getColor(R.color.game_answer_incorrect_text, null);
            private int incorrect_bg = getResources().getColor(R.color.game_answer_incorrect_background, null);
            private Drawable default_bg = getResources().getDrawable(R.drawable.game_answer_button_background, null);
            private int default_text = getResources().getColor(R.color.game_answer_text, null);

            AnswerViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            void bind(QuizAnswer answer, BiConsumer<AnswerViewHolder, QuizAnswer> handler) {
                TextView answerView = itemView.findViewById(R.id.game_answer_list_item_answer_text);
                answerView.setText(answer.text);
                answerView.setOnClickListener((v) -> handler.accept(this, answer));
                reset();
            }

            void reset() {
                TextView answerView = itemView.findViewById(R.id.game_answer_list_item_answer_text);
                answerView.setTextColor(default_text);
                answerView.setBackground(default_bg);
            }

            void displayAsCorrect() {
                TextView answerView = itemView.findViewById(R.id.game_answer_list_item_answer_text);
                answerView.setTextColor(correct_text);
                answerView.setBackgroundColor(correct_bg);
            }

            void displayAsIncorrect() {
                TextView answerView = itemView.findViewById(R.id.game_answer_list_item_answer_text);
                answerView.setTextColor(incorrect_text);
                answerView.setBackgroundColor(incorrect_bg);
            }
        }
    }

}
