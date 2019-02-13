package me.srikavin.quiz.view.game

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import me.srikavin.quiz.R
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel

@Suppress("PrivatePropertyName")
internal class AnswerViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {
    private val correct_text: Int
    private val correct_bg: Int
    private val chosen_correct_text: Int
    private val chosen_correct_bg: Int
    private val chosen_incorrect_text: Int
    private val chosen_incorrect_bg: Int
    private val incorrect_text: Int
    private val incorrect_bg: Int
    private val default_bg: Drawable
    private val default_text: Int

    init {
        @ColorInt
        fun getColor(@ColorRes color: Int): Int {
            return ContextCompat.getColor(context, color)
        }

        correct_text = getColor(R.color.game_answer_correct_text)
        correct_bg = getColor(R.color.game_answer_correct_background)

        chosen_correct_text = getColor(R.color.game_answer_chosen_correct_text)
        chosen_correct_bg = getColor(R.color.game_answer_chosen_correct_background)

        chosen_incorrect_text = getColor(R.color.game_answer_chosen_incorrect_text)
        chosen_incorrect_bg = getColor(R.color.game_answer_chosen_incorrect_background)

        incorrect_text = getColor(R.color.game_answer_incorrect_text)
        incorrect_bg = getColor(R.color.game_answer_incorrect_background)

        default_bg = context.resources.getDrawable(R.drawable.game_answer_button_background, null)
        default_text = getColor(R.color.game_answer_text)
    }

    fun bind(answer: QuizAnswerModel, handler: (AnswerViewHolder, QuizAnswerModel) -> Unit) {
        val answerView = itemView.findViewById<TextView>(R.id.game_answer_list_item_answer_text)
        answerView.text = answer.contents
        answerView.setOnClickListener { handler(this, answer) }
    }

    fun reset() {
        val answerView = itemView.findViewById<TextView>(R.id.game_answer_list_item_answer_text)
        answerView.setTextColor(default_text)
        answerView.background = default_bg
        answerView.setTypeface(null, Typeface.NORMAL)
    }

    fun displayAsCorrect() {
        val answerView = itemView.findViewById<TextView>(R.id.game_answer_list_item_answer_text)
        answerView.setTextColor(correct_text)
        answerView.setBackgroundColor(correct_bg)
    }

    fun displayAsIncorrect() {
        val answerView = itemView.findViewById<TextView>(R.id.game_answer_list_item_answer_text)
        answerView.setTextColor(incorrect_text)
        answerView.setBackgroundColor(incorrect_bg)
    }

    fun displayAsChosenCorrect() {
        val answerView = itemView.findViewById<TextView>(R.id.game_answer_list_item_answer_text)
        answerView.setTextColor(chosen_correct_text)
        answerView.setBackgroundColor(chosen_correct_bg)
        answerView.setTypeface(null, Typeface.BOLD_ITALIC)
    }

    fun displayAsChosenIncorrect() {
        val answerView = itemView.findViewById<TextView>(R.id.game_answer_list_item_answer_text)
        answerView.setTextColor(chosen_incorrect_text)
        answerView.setTypeface(null, Typeface.BOLD_ITALIC)
        answerView.setBackgroundColor(chosen_incorrect_bg)
    }
}
