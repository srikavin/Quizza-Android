package me.srikavin.quiz.view.detail

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import me.srikavin.quiz.R

class QuizDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quiz_detail_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(
                            R.id.container,
                            QuizDetailFragment.newInstance(intent.getStringExtra(ARG_QUIZ_ID), intent.getStringExtra(ARG_QUIZ_NAME))
                    )
                    .commitNow()
        }
    }
}
