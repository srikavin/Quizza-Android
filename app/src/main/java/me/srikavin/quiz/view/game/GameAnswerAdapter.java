package me.srikavin.quiz.view.game;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java9.util.function.BiConsumer;
import java9.util.function.Consumer;
import java9.util.stream.StreamSupport;
import me.srikavin.quiz.R;
import me.srikavin.quiz.model.QuizAnswer;

class GameAnswerAdapter extends RecyclerView.Adapter<AnswerViewHolder> {

    public BiConsumer<AnswerViewHolder, QuizAnswer> onBindAnswer = (g, q) -> {
    };
    private List<QuizAnswer> answers = Collections.emptyList();
    private AnswerViewHolder lastChosen;
    private RecyclerView recycler;
    private Consumer<QuizAnswer> onClick;
    private Context context;

    public GameAnswerAdapter(RecyclerView recycler, Consumer<QuizAnswer> onClick, Context context) {
        this.recycler = recycler;
        this.onClick = onClick;
        this.context = context;
    }

    public void setOnBindAnswer(BiConsumer<AnswerViewHolder, QuizAnswer> onBindAnswer) {
        this.onBindAnswer = onBindAnswer;
    }

    @NonNull
    @Override
    public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AnswerViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.game_answer_list_item, parent, false), context);
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
        holder.reset();
        QuizAnswer answer = answers.get(position);
        holder.bind(answer, (vh, qa) -> {
            onClick.accept(qa);
            lastChosen = vh;
        });
        onBindAnswer.accept(holder, answer);
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

    void displayAsChosenIncorrect(QuizAnswer answer) {
        AnswerViewHolder holder = ((AnswerViewHolder) recycler.findViewHolderForAdapterPosition(answers.indexOf(answer)));
        if (holder != null) {
            holder.displayAsChosenIncorrect();
        }
    }

    void displayAsChosenCorrect(QuizAnswer answer) {
        AnswerViewHolder holder = ((AnswerViewHolder) recycler.findViewHolderForAdapterPosition(answers.indexOf(answer)));
        if (holder != null) {
            holder.displayAsChosenCorrect();
        }
    }

    void displayAsIncorrect(QuizAnswer answer) {
        AnswerViewHolder holder = ((AnswerViewHolder) recycler.findViewHolderForAdapterPosition(answers.indexOf(answer)));
        if (holder != null) {
            holder.displayAsIncorrect();
        }
    }

    void displayAsCorrect(QuizAnswer answer) {
        AnswerViewHolder holder = ((AnswerViewHolder) recycler.findViewHolderForAdapterPosition(answers.indexOf(answer)));
        if (holder != null) {
            holder.displayAsCorrect();
        }
    }

    void displayCorrectAnswers(List<QuizAnswer> correct) {
        StreamSupport.stream(answers).filter(correct::contains).forEach(this::displayAsCorrect);
    }

}
