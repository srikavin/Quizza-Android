package me.srikavin.quiz.view.game

import android.app.ProgressDialog
import android.content.DialogInterface
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
import me.srikavin.quiz.viewmodel.GameViewModel
import java.util.concurrent.atomic.AtomicInteger

class GameFragment : Fragment() {

    private lateinit var viewModel: GameViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.game_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(GameViewModel::class.java)

        val title = view.findViewById<TextView>(R.id.game_question_title)
        val answerRecycler = view.findViewById<RecyclerView>(R.id.game_question_answer_list)
        val countdownBar = view.findViewById<ProgressBar>(R.id.game_time_progress_bar)
        val countdownText = view.findViewById<TextView>(R.id.game_time_countdown)
        val gamePosition = view.findViewById<TextView>(R.id.game_position)
        val container = view.findViewById<ViewGroup>(R.id.game_container)

        val adapter = GameAnswerAdapter(answerRecycler, { viewModel.submitAnswer(it) }, requireContext())

        answerRecycler.layoutManager = LinearLayoutManager(context)
        answerRecycler.adapter = adapter

        val args = arguments ?: throw IllegalStateException("Arguments must not be null!")

        val id = args.getString(ARG_GAME_ID)
                ?: throw IllegalArgumentException("Id is required to create a GameFragment")

        val remote = args.getBoolean(ARG_GAME_REMOTE, false)


        val numberOfQuestions = AtomicInteger()
        val currentQuestion = AtomicInteger()

        countdownText.text = getString(R.string.waiting_for_players)

        val dialog = ProgressDialog(requireContext())
        dialog.setTitle("Searching for players")
        dialog.setMessage("1 / 2 players found")
        dialog.setCancelable(false)
        dialog.isIndeterminate = true

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { _, _ ->
            viewModel.stopMatchmaking()
            viewModel.quitGame()
            if (activity != null) {
                activity!!.finish()
            }
        }

        dialog.show()

        viewModel.getQuizByID(id).observe(this, Observer { quiz ->
            if (quiz == null) {
                Toast.makeText(activity, "Failed to load quiz", Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
            viewModel.createGame(quiz!!, remote)
        })

        viewModel.getCurrentGameID().observe(this, Observer {
            dialog.dismiss()
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
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.quitGame()
    }

    private fun displayStatsFragment(stats: GameRepository.GameStats) {
        (activity as GameActivity).goToStatsDisplay(stats)
    }

    companion object {
        const val ARG_GAME_ID = "GAME_ID"
        const val ARG_GAME_REMOTE = "GAME_IS_REMOTE"

        fun newInstance(id: String, isRemote: Boolean): GameFragment {
            val fragment = GameFragment()
            val bundle = Bundle()
            bundle.putString(ARG_GAME_ID, id)
            bundle.putBoolean(ARG_GAME_REMOTE, isRemote)
            fragment.arguments = bundle
            return fragment
        }
    }

}
