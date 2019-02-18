package me.srikavin.quiz.view.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.srikavin.quiz.R
import me.srikavin.quiz.network.common.model.data.QuizQuestionModel

class QuestionRecyclerAdapter(
        questions: List<QuizQuestionModel>,
        private val context: Context
) : RecyclerView.Adapter<QuestionRecyclerViewHolder>() {
    var questions: List<QuizQuestionModel> = questions
        set(value) {
            field = value
            println(questions)
            this.notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionRecyclerViewHolder {
        return QuestionRecyclerViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.quiz_detail_question_item, parent, false))
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun onBindViewHolder(holder: QuestionRecyclerViewHolder, position: Int) {
        holder.bind(questions[position])
    }

}

class QuestionRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val questionTitle: TextView = itemView.findViewById(R.id.quiz_detail_question_text)

    fun bind(question: QuizQuestionModel) {
        questionTitle.text = question.contents
    }
}