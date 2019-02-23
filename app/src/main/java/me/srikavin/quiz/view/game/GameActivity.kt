package me.srikavin.quiz.view.game

import android.os.Bundle

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import me.srikavin.quiz.R
import me.srikavin.quiz.repository.GameRepository

class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, GameFragment.newInstance(intent.getStringExtra(EXTRA_QUIZ_ID), intent.getBooleanExtra(
                            ONLINE,
                            false
                    )))
                    .commitNow()
        }
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Return to Home")
        builder.setMessage("Are you sure you want to return to the main screen?")
        builder.setPositiveButton("Exit") { dialog, _ ->
            super.onBackPressed()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()

    }

    internal fun goToStatsDisplay(stats: GameRepository.GameStats) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, GameStatsFragment.newInstance(stats))
                .commitNow()
    }

    companion object {
        const val EXTRA_QUIZ_ID = "QUIZ_ID"
        const val ONLINE = "QUIZ_ONLINE"
    }
}
