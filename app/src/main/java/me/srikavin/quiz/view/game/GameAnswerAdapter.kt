package me.srikavin.quiz.view.game

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.srikavin.quiz.R
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel

internal typealias AnswerBindHandler = (AnswerViewHolder, QuizAnswerModel) -> Unit

internal class GameAnswerAdapter(private val recycler: RecyclerView, private val onClick: (QuizAnswerModel) -> Unit, private val context: Context) : RecyclerView.Adapter<AnswerViewHolder>() {
    var answerBindHandler: AnswerBindHandler = { _, _ -> }
    private var answers = emptyList<QuizAnswerModel>()
    private var lastChosen: AnswerViewHolder? = null

    fun setOnBindAnswer(onBindAnswer: AnswerBindHandler) {
        this.answerBindHandler = onBindAnswer
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        return AnswerViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.game_answer_list_item, parent, false), context)
    }

    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        holder.reset()
        val answer = answers[position]
        holder.bind(answer) { viewHolder, quizAnswer ->
            onClick(quizAnswer)
            lastChosen = viewHolder
        }
        answerBindHandler(holder, answer)
    }

    override fun getItemCount(): Int {
        return answers.size
    }

    fun setAnswers(answers: List<QuizAnswerModel>) {
        this.answers = answers
        notifyDataSetChanged()
    }

    fun displayLastAsCorrect() {
        if (lastChosen != null) {
            this.lastChosen!!.displayAsCorrect()
        }
    }

    fun displayLastAsIncorrect() {
        if (lastChosen != null) {
            lastChosen!!.displayAsIncorrect()
        }
    }

    fun displayAsChosenIncorrect(answer: QuizAnswerModel) {
        val holder = recycler.findViewHolderForAdapterPosition(answers.indexOf(answer)) as AnswerViewHolder?
        holder?.displayAsChosenIncorrect()
    }

    fun displayAsChosenCorrect(answer: QuizAnswerModel) {
        val holder = recycler.findViewHolderForAdapterPosition(answers.indexOf(answer)) as AnswerViewHolder?
        holder?.displayAsChosenCorrect()
    }

    fun displayAsIncorrect(answer: QuizAnswerModel) {
        val holder = recycler.findViewHolderForAdapterPosition(answers.indexOf(answer)) as AnswerViewHolder?
        holder?.displayAsIncorrect()
    }

    fun displayAsCorrect(answer: QuizAnswerModel) {
        val holder = recycler.findViewHolderForAdapterPosition(answers.indexOf(answer)) as AnswerViewHolder?
        holder?.displayAsCorrect()
    }

    fun displayCorrectAnswers(correct: List<QuizAnswerModel>) {
        answers.filter { correct.contains(it) }.forEach { this.displayAsCorrect(it) }
    }

}
