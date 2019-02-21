package me.srikavin.quiz.view.game

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.jinatonic.confetti.CommonConfetti
import com.google.gson.Gson
import me.srikavin.quiz.R
import me.srikavin.quiz.network.common.model.data.QuizAnswerModel
import me.srikavin.quiz.network.common.model.data.QuizQuestionModel
import me.srikavin.quiz.repository.GameRepository

class GameStatsFragment : Fragment() {

    private lateinit var stats: GameRepository.GameStats

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.game_finished_stats, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        stats = arguments!!.getSerializable(ARG_STATS_JSON) as GameRepository.GameStats

        val correctFraction = view!!.findViewById<TextView>(R.id.game_stats_correct_fraction)
        val correctPercentage = view!!.findViewById<TextView>(R.id.game_stats_correct_percentage)

        correctFraction.text = getString(R.string.game_stats_correct_fraction_text, stats.correct, stats.total)
        correctPercentage.text = getString(R.string.game_stats_correct_percentage_text, stats.percentCorrect * 100)

        val adapter = QuestionAdapter(stats)

        val recyclerView = view!!.findViewById<RecyclerView>(R.id.game_stats_questions)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        if (stats.percentCorrect > 0.85) {
            Handler().postDelayed({
                CommonConfetti.rainingConfetti(view!!.findViewById(R.id.game_stats_confetti_container), intArrayOf(Color.GREEN, Color.CYAN, ContextCompat.getColor(context!!, R.color.colorPrimary), ContextCompat.getColor(context!!, R.color.colorSecondary))).confettiManager
                        .setEmissionDuration((50 + stats.score).toLong())
                        .setVelocityY(600f, 75f)
                        .setVelocityX(0f, 200f)
                        .setEmissionRate(40 + stats.score * .25f).animate()
            }, 1200)
        }
    }

    private inner class QuestionAdapter(stats: GameRepository.GameStats) : RecyclerView.Adapter<QuestionCard>() {
        private val questions: List<QuizQuestionModel> = stats.quizQuestions
        private val chosen: List<QuizAnswerModel?> = stats.chosen

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionCard {
            return QuestionCard(LayoutInflater.from(context).inflate(R.layout.game_question_card, parent, false))
        }

        override fun onBindViewHolder(holder: QuestionCard, position: Int) {
            holder.bind(questions[position], chosen[position])
        }

        override fun getItemCount(): Int {
            return questions.size
        }
    }

    private inner class QuestionCard internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal fun bind(question: QuizQuestionModel, chosen: QuizAnswerModel?) {
            val recyclerView = itemView.findViewById<RecyclerView>(R.id.game_question_answer_list)
            val title = itemView.findViewById<TextView>(R.id.game_question_title)

            val answerAdapter = GameAnswerAdapter(recyclerView, {}, context!!)

            title.text = question.contents
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = answerAdapter

            answerAdapter.setAnswers(question.answers)

            val correct = question.answers.filter { it.isCorrect }.toList()

            answerAdapter.setOnBindAnswer { viewHolder, answer ->
                if (chosen == answer) {
                    if (chosen.isCorrect) {
                        viewHolder.displayAsChosenCorrect()
                    } else {
                        viewHolder.displayAsChosenIncorrect()
                    }
                } else {
                    if (correct.contains(answer)) {
                        viewHolder.displayAsCorrect()
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_STATS_JSON = "stats_json"
        private val gson = Gson()

        fun newInstance(stats: GameRepository.GameStats): GameStatsFragment {
            val fragment = GameStatsFragment()
            val args = Bundle()
            args.putSerializable(ARG_STATS_JSON, stats)
            fragment.arguments = args
            return fragment
        }
    }
}
