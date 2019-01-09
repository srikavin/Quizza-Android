package me.srikavin.quiz.view.game;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java9.util.function.BiConsumer;
import me.srikavin.quiz.R;
import me.srikavin.quiz.model.QuizAnswer;

class AnswerViewHolder extends RecyclerView.ViewHolder {
    private int correct_text;
    private int correct_bg;
    private int chosen_correct_text;
    private int chosen_correct_bg;
    private int chosen_incorrect_text;
    private int chosen_incorrect_bg;
    private int incorrect_text;
    private int incorrect_bg;
    private Drawable default_bg;
    private int default_text;

    AnswerViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        correct_text = context.getResources().getColor(R.color.game_answer_correct_text, null);
        correct_bg = context.getResources().getColor(R.color.game_answer_correct_background, null);

        chosen_correct_text = context.getResources().getColor(R.color.game_answer_chosen_correct_text, null);
        chosen_correct_bg = context.getResources().getColor(R.color.game_answer_chosen_correct_background, null);

        chosen_incorrect_text = context.getResources().getColor(R.color.game_answer_chosen_incorrect_text, null);
        chosen_incorrect_bg = context.getResources().getColor(R.color.game_answer_chosen_incorrect_background, null);

        incorrect_text = context.getResources().getColor(R.color.game_answer_incorrect_text, null);
        incorrect_bg = context.getResources().getColor(R.color.game_answer_incorrect_background, null);

        default_bg = context.getResources().getDrawable(R.drawable.game_answer_button_background, null);
        default_text = context.getResources().getColor(R.color.game_answer_text, null);
    }

    void bind(QuizAnswer answer, BiConsumer<AnswerViewHolder, QuizAnswer> handler) {
        TextView answerView = itemView.findViewById(R.id.game_answer_list_item_answer_text);
        answerView.setText(answer.text);
        answerView.setOnClickListener((v) -> handler.accept(this, answer));
    }

    void reset() {
        TextView answerView = itemView.findViewById(R.id.game_answer_list_item_answer_text);
        answerView.setTextColor(default_text);
        answerView.setBackground(default_bg);
        answerView.setTypeface(null, Typeface.NORMAL);
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

    void displayAsChosenCorrect() {
        TextView answerView = itemView.findViewById(R.id.game_answer_list_item_answer_text);
        answerView.setTextColor(chosen_correct_text);
        answerView.setBackgroundColor(chosen_correct_bg);
        answerView.setTypeface(null, Typeface.BOLD_ITALIC);
    }

    void displayAsChosenIncorrect() {
        TextView answerView = itemView.findViewById(R.id.game_answer_list_item_answer_text);
        answerView.setTextColor(chosen_incorrect_text);
        answerView.setTypeface(null, Typeface.BOLD_ITALIC);
        answerView.setBackgroundColor(chosen_incorrect_bg);
    }
}
