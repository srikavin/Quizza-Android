package me.srikavin.quiz.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.srikavin.quiz.R

class QuizEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quiz_edit_activity)
        val intent = intent
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container,
                            QuizEditFragment.newInstance(intent.getSerializableExtra("mode") as QuizEditFragment.Mode,
                                    intent.getStringExtra("id")))
                    .commitNow()
        }
    }
}
