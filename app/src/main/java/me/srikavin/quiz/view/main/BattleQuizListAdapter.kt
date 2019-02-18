package me.srikavin.quiz.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import me.srikavin.quiz.R
import me.srikavin.quiz.model.Quiz

typealias BattleListOnClickHandler = (quiz: Quiz, vh: QuizListViewHolder, position: Int) -> Unit

class BattleQuizListAdapter(private val onClick: BattleListOnClickHandler, quizzes: List<Quiz>) : RecyclerView.Adapter<QuizListViewHolder>() {
    var quizzes = quizzes
        set(value) {
            field = value
            this.notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizListViewHolder {
        return QuizListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.battle_grid_item, parent, false))
    }

    override fun onBindViewHolder(holder: QuizListViewHolder, position: Int) {
        holder.setQuiz(quizzes[position], position)
        holder.container.setOnClickListener {
            onClick(quizzes[position], holder, position)
        }
    }

    override fun getItemCount(): Int {
        return quizzes.size
    }
}

class QuizListViewHolder(val container: View) : RecyclerView.ViewHolder(container) {
    val image: ImageView = container.findViewById(R.id.battle_grid_item_image)
    val title: TextView = container.findViewById(R.id.battle_grid_item_title)

    fun setQuiz(quiz: Quiz, position: Int) {
        this.title.text = quiz.title
        Picasso.get().load(quiz.coverImage).into(image)
        image.transitionName = "quiz_image_$position"
        title.transitionName = "quiz_title_$position"
    }
}
