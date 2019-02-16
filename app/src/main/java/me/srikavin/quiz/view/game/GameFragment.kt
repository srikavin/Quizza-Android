package me.srikavin.quiz.view.game

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.jinatonic.confetti.CommonConfetti
import me.srikavin.quiz.R
import me.srikavin.quiz.model.QuizGameState
import me.srikavin.quiz.repository.GameRepository
import me.srikavin.quiz.view.main.MainActivity
import me.srikavin.quiz.viewmodel.GameViewModel
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class GameFragment : Fragment() {

    private lateinit var viewModel: GameViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.game_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GameViewModel::class.java)

        val title = view!!.findViewById<TextView>(R.id.game_question_title)
        val answerRecycler = view!!.findViewById<RecyclerView>(R.id.game_question_answer_list)
        val countdownBar = view!!.findViewById<ProgressBar>(R.id.game_time_progress_bar)
        val countdownText = view!!.findViewById<TextView>(R.id.game_time_countdown)
        val gamePosition = view!!.findViewById<TextView>(R.id.game_position)
        val container = view!!.findViewById<ViewGroup>(R.id.game_container)

        val adapter = GameAnswerAdapter(answerRecycler, { viewModel.submitAnswer(it) }, requireContext())

        answerRecycler.layoutManager = LinearLayoutManager(context)
        answerRecycler.adapter = adapter

        val id = arguments?.getString("id")
                ?: throw IllegalArgumentException("Id is required to create a GameFragment")

        val numberOfQuestions = AtomicInteger()
        val currentQuestion = AtomicInteger()

        viewModel.getQuizByID(id).observe(this, Observer { quiz ->
            if (quiz == null) {
                Toast.makeText(activity, "Failed to load quiz", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            viewModel.createGame(quiz!!)
        })

        viewModel.getGameInfo().observe(this, Observer { info ->
            countdownBar.max = info.timePerQuestion
            numberOfQuestions.set(info.numberOfQuestions)
            gamePosition.text = getString(R.string.game_position, currentQuestion.get(), numberOfQuestions.get())
        })

        viewModel.getCurrentQuestion().observe(this, Observer { question ->
            adapter.setAnswers(question.answers)
            title.text = question.contents
        })
        viewModel.getTimeRemaining().observe(this, Observer { timeLeft ->
            countdownText.text = "$timeLeft"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                countdownBar.setProgress(timeLeft!!, true)
            } else {
                countdownBar.progress = timeLeft!!
            }
        })

        viewModel.getGameState().observe(this, Observer { state ->
            currentQuestion.set(state.currentQuestion + 1)
            gamePosition.text = getString(R.string.game_position, currentQuestion.get(), numberOfQuestions.get())
        })

        val context = context

        viewModel.getAnswerResponse().observe(this, Observer { response ->
            adapter.displayCorrectAnswers(response.correctAnswers)
            if (response.isCorrect) {
                // The chosen answer was correct
                CommonConfetti.rainingConfetti(container, intArrayOf(Color.GREEN, Color.CYAN, ContextCompat.getColor(context!!, R.color.colorPrimary), ContextCompat.getColor(context, R.color.colorSecondary))).confettiManager
                        .setEmissionDuration((750 + countdownBar.progress * 10).toLong())
                        .setVelocityY(600f, 75f)
                        .setVelocityX(0f, 200f)
                        .setEmissionRate(countdownBar.progress * 2f).animate()
                adapter.displayLastAsCorrect()
            } else {
                // The chosen answer was wrong
                if (countdownBar.progress > 0) {
                    adapter.displayLastAsIncorrect()
                }
            }
        })

        viewModel.getGameStats().observe(this, Observer { stats ->
            if (viewModel.getGameState().value == QuizGameState.FINISHED) {
                displayStatsFragment(stats)
            }
        })

        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (activity == null) {
                    return
                }
                activity!!.runOnUiThread {
                    if (viewModel.getGameState().value == QuizGameState.FINISHED) {
                        val stats = viewModel.getGameStats().value ?: return@runOnUiThread
                        displayStatsFragment(stats)
                    }
                }
            }
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.quitGame()
    }

    private fun displayStatsFragment(stats: GameRepository.GameStats) {
        (activity as GameActivity).goToStatsDisplay(stats)
    }

    companion object {

        fun newInstance(id: String): GameFragment {
            val fragment = GameFragment()
            val bundle = Bundle()
            bundle.putString("id", id)
            fragment.arguments = bundle
            return fragment
        }
    }

}
